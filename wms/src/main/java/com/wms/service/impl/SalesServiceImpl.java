package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.Sales;
import com.wms.entity.SalesDTO;
import com.wms.entity.SalesDetail;
import com.wms.entity.SalesDetailVO;
import com.wms.entity.SalesReturn;
import com.wms.entity.SalesVO;
import com.wms.entity.Record;
import com.wms.mapper.GoodsBatchMapper;
import com.wms.mapper.SalesDetailMapper;
import com.wms.mapper.SalesMapper;
import com.wms.mapper.SalesReturnMapper;
import com.wms.service.IGoodsService;
import com.wms.service.ISalesDetailService;
import com.wms.service.ISalesService;
import com.wms.service.IRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalesServiceImpl extends ServiceImpl<SalesMapper, Sales> implements ISalesService {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISalesDetailService salesDetailService;

    @Autowired
    private SalesMapper salesMapper;

    @Autowired
    private SalesDetailMapper salesDetailMapper;

    @Autowired
    private SalesReturnMapper salesReturnMapper;

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;

    @Autowired
    private IRecordService recordService;

    /**
     * 收银结算 — FIFO批次出库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer checkout(SalesDTO salesDTO) {
        List<SalesDetail> details = salesDTO.getDetails();
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("销售明细不能为空");
        }

        // 1. 遍历明细，FIFO逐批扣减
        for (SalesDetail detail : details) {
            Goods goods = goodsService.getById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + detail.getGoodsId() + "】不存在");
            }

            // 可用库存校验
            int reserved = goods.getReservedCount() != null ? goods.getReservedCount() : 0;
            int available = goods.getCount() - reserved;
            if (available < detail.getCount()) {
                throw new RuntimeException("商品【" + goods.getName() + "】库存不足，可用:" + available + "，需求:" + detail.getCount());
            }

            // FIFO逐批扣减
            int remaining = detail.getCount();
            StringBuilder batchNos = new StringBuilder();
            LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
            batchQuery.eq(GoodsBatch::getGoodsId, detail.getGoodsId())
                      .gt(GoodsBatch::getCurrentCount, 0)
                      .orderByAsc(GoodsBatch::getCreateTime);
            List<GoodsBatch> batches = goodsBatchMapper.selectList(batchQuery);
            for (GoodsBatch batch : batches) {
                if (remaining <= 0) break;
                int deduct = Math.min(batch.getCurrentCount(), remaining);
                batch.setCurrentCount(batch.getCurrentCount() - deduct);
                goodsBatchMapper.updateById(batch);
                if (batchNos.length() > 0) batchNos.append(",");
                batchNos.append(batch.getBatchNo());
                remaining -= deduct;
            }
            // 批次库存不足（批次总和 < goods.count），创建超卖兜底批次
            if (remaining > 0) {
                GoodsBatch dummy = new GoodsBatch();
                dummy.setGoodsId(goods.getId());
                dummy.setBatchNo("SALE" + System.currentTimeMillis());
                dummy.setStorageId(goods.getStorage());
                dummy.setSupplierId(goods.getSupplierId());
                dummy.setPurchasePrice(goods.getPurchasePrice() != null ? goods.getPurchasePrice() : BigDecimal.ZERO);
                dummy.setInitialCount(0);
                dummy.setCurrentCount(-remaining);
                dummy.setCreateTime(LocalDateTime.now());
                goodsBatchMapper.insert(dummy);
                if (batchNos.length() > 0) batchNos.append(",");
                batchNos.append(dummy.getBatchNo());
                remaining = 0;
            }

            // 记录出库批次号到明细
            detail.setBatchNo(batchNos.toString());
            detail.setStatus(0); // 正常

            // 扣减goods总库存
            goods.setCount(goods.getCount() - detail.getCount());
            goodsService.updateById(goods);
        }

        // 2. 插入销售主表
        Sales sales = new Sales();
        sales.setUserId(salesDTO.getUserId());
        sales.setTotalAmount(salesDTO.getTotalAmount());
        sales.setRealAmount(salesDTO.getRealAmount());
        sales.setChangeAmount(salesDTO.getChangeAmount());
        sales.setPaymentMethod(salesDTO.getPaymentMethod() != null ? salesDTO.getPaymentMethod() : 1);
        sales.setStatus(0); // 正常
        sales.setCreateTime(LocalDateTime.now());

        if (!this.save(sales)) {
            throw new RuntimeException("销售单保存失败");
        }

        Integer salesId = sales.getId();
        String orderNum = "SO" + String.format("%08d", salesId);
        sales.setOrderNum(orderNum);
        this.updateById(sales);

        // 3. 插入明细
        for (SalesDetail detail : details) {
            detail.setSalesId(salesId);
        }
        salesDetailService.saveBatch(details);

        // 4. 查询进价并记录流水（含批次号+进价）
        for (SalesDetail detail : details) {
            Goods goods = goodsService.getById(detail.getGoodsId());

            // 汇总计算加权成本
            StringBuilder costDetail = new StringBuilder();
            LambdaQueryWrapper<GoodsBatch> costQuery = new LambdaQueryWrapper<>();
            costQuery.eq(GoodsBatch::getGoodsId, detail.getGoodsId())
                     .orderByAsc(GoodsBatch::getCreateTime);
            List<GoodsBatch> allBatches = goodsBatchMapper.selectList(costQuery);

            Record record = new Record();
            record.setGoods(detail.getGoodsId());
            record.setCount(-detail.getCount());
            record.setOperationType("销售出库");
            record.setRefOrderNum(orderNum);
            record.setAdminId(sales.getUserId());
            record.setCreatetime(sales.getCreateTime());
            record.setStatus(1);
            record.setRemark("POS销售，批次:" + detail.getBatchNo()
                    + "，售价:" + (detail.getPrice() != null ? detail.getPrice() : "-"));
            recordService.save(record);
        }

        return salesId;
    }

    @Override
    public IPage<SalesVO> listPage(Page<SalesVO> page, String orderNum, String startDate, String endDate, Integer userId) {
        return salesMapper.selectSalesPage(page, orderNum, startDate, endDate, userId);
    }

    @Override
    public List<SalesDetailVO> getDetails(Integer salesId) {
        return salesDetailMapper.selectDetailsBySalesId(salesId);
    }

    /**
     * 收银台直接退款 — 回滚批次库存 + 更新状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refund(String orderNum) {
        LambdaQueryWrapper<Sales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sales::getOrderNum, orderNum);
        Sales sales = this.getOne(queryWrapper);

        if (sales == null) {
            throw new RuntimeException("销售单【" + orderNum + "】不存在");
        }
        if (sales.getStatus() != null && sales.getStatus() == 1) {
            throw new RuntimeException("该销售单已退款");
        }

        Integer salesId = sales.getId();

        // 查询明细
        LambdaQueryWrapper<SalesDetail> detailQuery = new LambdaQueryWrapper<>();
        detailQuery.eq(SalesDetail::getSalesId, salesId);
        List<SalesDetail> details = salesDetailService.list(detailQuery);
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("销售明细不存在");
        }

        // 遍历明细，回滚批次库存 + 商品总库存
        for (SalesDetail detail : details) {
            Goods goods = goodsService.getById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + detail.getGoodsId() + "】不存在");
            }

            // 恢复goods.count
            goods.setCount(goods.getCount() + detail.getCount());
            goodsService.updateById(goods);

            // 恢复goods_batch：将数量追加到最近批次
            LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
            batchQuery.eq(GoodsBatch::getGoodsId, detail.getGoodsId())
                      .orderByDesc(GoodsBatch::getCreateTime).last("LIMIT 1");
            List<GoodsBatch> batches = goodsBatchMapper.selectList(batchQuery);
            if (batches != null && !batches.isEmpty()) {
                GoodsBatch latest = batches.get(0);
                latest.setCurrentCount(latest.getCurrentCount() + detail.getCount());
                goodsBatchMapper.updateById(latest);
            }

            // 更新明细状态
            detail.setStatus(1); // 退货
            salesDetailMapper.updateById(detail);

            // 流水
            Record record = new Record();
            record.setGoods(detail.getGoodsId());
            record.setCount(detail.getCount());
            record.setOperationType("销售退货");
            record.setRefOrderNum("REFUND_" + orderNum);
            record.setAdminId(sales.getUserId());
            record.setCreatetime(LocalDateTime.now());
            record.setStatus(1);
            record.setRemark("POS退款，原批次:" + (detail.getBatchNo() != null ? detail.getBatchNo() : "-"));
            recordService.save(record);
        }

        // 更新主表状态
        sales.setStatus(1); // 已退款
        this.updateById(sales);

        // 创建退货记录
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnNo(sales.getOrderNum());
        salesReturn.setReturnNum("REF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        salesReturn.setSalesId(salesId);
        salesReturn.setReturnReason("收银台直接退款");
        salesReturn.setReturnAmount(sales.getRealAmount());
        salesReturn.setStatus(1);
        salesReturn.setCreateTime(LocalDateTime.now());
        salesReturn.setRefundTime(LocalDateTime.now());
        salesReturn.setUserId(sales.getUserId());
        salesReturnMapper.insert(salesReturn);

        return true;
    }
}
