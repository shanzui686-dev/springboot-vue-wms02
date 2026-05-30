package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.*;
import com.wms.mapper.GoodsBatchMapper;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.LossReportMapper;
import com.wms.mapper.SalesReturnDetailMapper;
import com.wms.mapper.SalesReturnMapper;
import com.wms.service.IGoodsService;
import com.wms.service.ISalesReturnDetailService;
import com.wms.service.ISalesReturnService;
import com.wms.service.ISalesService;
import com.wms.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalesReturnServiceImpl extends ServiceImpl<SalesReturnMapper, SalesReturn> implements ISalesReturnService {

    @Autowired
    private ISalesService salesService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISalesReturnDetailService salesReturnDetailService;

    @Autowired
    private SalesReturnMapper salesReturnMapper;

    @Autowired
    private SalesReturnDetailMapper salesReturnDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;

    @Autowired
    private LossReportMapper lossReportMapper;

    @Autowired
    private IRecordService recordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyReturn(ReturnApplyDTO returnApplyDTO) {
        if (returnApplyDTO.getSalesId() == null) {
            throw new RuntimeException("原销售单ID不能为空");
        }
        if (returnApplyDTO.getItems() == null || returnApplyDTO.getItems().isEmpty()) {
            throw new RuntimeException("退货商品明细不能为空");
        }

        Sales sales = salesService.getById(returnApplyDTO.getSalesId());
        if (sales == null) {
            throw new RuntimeException("原销售单不存在");
        }

        // 一个销售单只允许一条退货记录（一一对应）
        LambdaQueryWrapper<SalesReturn> existCheck = new LambdaQueryWrapper<>();
        existCheck.eq(SalesReturn::getSalesId, returnApplyDTO.getSalesId());
        if (this.count(existCheck) > 0) {
            throw new RuntimeException("该销售单已存在退货记录，不能重复退货");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnApplyDTO.ReturnItemDTO item : returnApplyDTO.getItems()) {
            Goods goods = goodsService.getById(item.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + item.getGoodsId() + "】不存在");
            }
            if (item.getReturnCount() == null || item.getReturnCount() <= 0) {
                throw new RuntimeException("商品【" + goods.getName() + "】退货数量必须大于0");
            }
            BigDecimal subtotal = goods.getRetailPrice()
                    .multiply(BigDecimal.valueOf(item.getReturnCount()));
            totalAmount = totalAmount.add(subtotal);
        }

        String returnNo = sales.getOrderNum() != null
                ? "TH" + sales.getOrderNum()
                : "TH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnNum(returnNo);
        salesReturn.setReturnNo(returnNo);
        salesReturn.setSalesId(returnApplyDTO.getSalesId());
        salesReturn.setReturnReason(returnApplyDTO.getReturnReason());
        salesReturn.setReturnAmount(totalAmount);
        salesReturn.setStatus(0);
        salesReturn.setCreateTime(LocalDateTime.now());
        salesReturn.setIsResalable(returnApplyDTO.getIsResalable() != null ? returnApplyDTO.getIsResalable() : 0);
        salesReturn.setType(returnApplyDTO.getType() != null ? returnApplyDTO.getType() : 1);

        if (!this.save(salesReturn)) {
            throw new RuntimeException("退货单保存失败");
        }

        Integer returnId = salesReturn.getId();

        List<SalesReturnDetail> detailList = new java.util.ArrayList<>();
        for (ReturnApplyDTO.ReturnItemDTO item : returnApplyDTO.getItems()) {
            Goods goods = goodsService.getById(item.getGoodsId());

            SalesReturnDetail detail = new SalesReturnDetail();
            detail.setReturnId(returnId);
            detail.setGoodsId(item.getGoodsId());
            detail.setReturnCount(item.getReturnCount());
            detail.setPrice(goods.getRetailPrice());
            detail.setSubtotal(goods.getRetailPrice().multiply(BigDecimal.valueOf(item.getReturnCount())));
            detail.setExchangeGoodsId(item.getExchangeGoodsId());

            detailList.add(detail);
        }

        if (!salesReturnDetailService.saveBatch(detailList)) {
            throw new RuntimeException("退货明细保存失败");
        }

        return returnId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmRefund(Integer returnId) {
        SalesReturn salesReturn = this.getById(returnId);
        if (salesReturn == null) {
            throw new RuntimeException("退货单不存在");
        }
        if (salesReturn.getStatus() == 1) {
            throw new RuntimeException("该退货单已退款，无需重复操作");
        }

        LambdaQueryWrapper<SalesReturnDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SalesReturnDetail::getReturnId, returnId);
        List<SalesReturnDetail> detailList = salesReturnDetailService.list(queryWrapper);
        if (detailList == null || detailList.isEmpty()) {
            throw new RuntimeException("退货明细不存在");
        }

        int isResalable = salesReturn.getIsResalable() != null ? salesReturn.getIsResalable() : 0;

        for (SalesReturnDetail detail : detailList) {
            Goods goods = goodsService.getById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + detail.getGoodsId() + "】不存在");
            }

            if (isResalable == 0) {
                // 可二次销售 → 恢复库存（goods_batch + goods）
                restoreBatchStock(goods, detail.getReturnCount());
                goods.setCount(goods.getCount() + detail.getReturnCount());
                goodsService.updateById(goods);

                Record record = new Record();
                record.setGoods(detail.getGoodsId());
                record.setCount(detail.getReturnCount());
                record.setOperationType("销售退货");
                record.setRefOrderNum(salesReturn.getReturnNum());
                record.setAdminId(salesReturn.getUserId());
                record.setCreatetime(LocalDateTime.now());
                record.setStatus(1);
                record.setRemark("退货入库（可二次销售）");
                recordService.save(record);

            } else {
                // 不可二次销售 → 退款但不恢复库存，自动生成损耗单
                LossReport lossReport = new LossReport();
                lossReport.setLossNo("LOSS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + (int)(Math.random() * 900000 + 100000));
                lossReport.setGoodsId(detail.getGoodsId());

                // 获取批次进价信息
                LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
                batchQuery.eq(GoodsBatch::getGoodsId, detail.getGoodsId())
                          .orderByDesc(GoodsBatch::getCreateTime).last("LIMIT 1");
                List<GoodsBatch> batches = goodsBatchMapper.selectList(batchQuery);
                if (batches != null && !batches.isEmpty()) {
                    GoodsBatch batch = batches.get(0);
                    lossReport.setBatchId(batch.getId());
                    lossReport.setBatchNo(batch.getBatchNo());
                    lossReport.setPurchasePrice(batch.getPurchasePrice());
                }

                lossReport.setLossCount(detail.getReturnCount());
                lossReport.setLossType(2); // 销售损耗
                if (lossReport.getPurchasePrice() != null) {
                    lossReport.setLossAmount(lossReport.getPurchasePrice()
                            .multiply(BigDecimal.valueOf(detail.getReturnCount()))
                            .setScale(2, RoundingMode.HALF_UP));
                }
                lossReport.setReason("销售退货，商品影响二次销售，自动生成损耗单。原退货单号:" + salesReturn.getReturnNum());
                lossReport.setStatus(0); // 待审核
                lossReport.setReporterId(salesReturn.getUserId());
                lossReport.setCreateTime(LocalDateTime.now());
                lossReportMapper.insert(lossReport);

                Record record = new Record();
                record.setGoods(detail.getGoodsId());
                record.setCount(detail.getReturnCount());
                record.setOperationType("销售退货");
                record.setRefOrderNum(salesReturn.getReturnNum());
                record.setAdminId(salesReturn.getUserId());
                record.setCreatetime(LocalDateTime.now());
                record.setStatus(1);
                record.setRemark("退货退款（不可二次销售，已生成损耗单" + lossReport.getLossNo() + "）");
                recordService.save(record);
            }
        }

        // 换货处理：扣减换货目标商品库存
        if (salesReturn.getType() != null && salesReturn.getType() == 2) {
            for (SalesReturnDetail detail : detailList) {
                if (detail.getExchangeGoodsId() == null) continue;
                Goods exchangeGoods = goodsService.getById(detail.getExchangeGoodsId());
                if (exchangeGoods == null) {
                    throw new RuntimeException("换货目标商品ID【" + detail.getExchangeGoodsId() + "】不存在");
                }
                // FIFO 扣减换货商品批次库存
                goodsService.deductStockFIFO(exchangeGoods.getId(), exchangeGoods.getStorage(), detail.getReturnCount());
                exchangeGoods.setCount(exchangeGoods.getCount() - detail.getReturnCount());
                goodsService.updateById(exchangeGoods);

                Record exchangeRecord = new Record();
                exchangeRecord.setGoods(detail.getExchangeGoodsId());
                exchangeRecord.setCount(-detail.getReturnCount());
                exchangeRecord.setOperationType("换货出库");
                exchangeRecord.setRefOrderNum(salesReturn.getReturnNum());
                exchangeRecord.setAdminId(salesReturn.getUserId());
                exchangeRecord.setCreatetime(LocalDateTime.now());
                exchangeRecord.setStatus(1);
                exchangeRecord.setRemark("换货出库，原商品ID:" + detail.getGoodsId() + "，数量:" + detail.getReturnCount());
                recordService.save(exchangeRecord);
            }
        }

        salesReturn.setStatus(1);
        salesReturn.setRefundTime(LocalDateTime.now());
        return this.updateById(salesReturn);
    }

    @Override
    public IPage<SalesReturnVO> listPage(Page<SalesReturnVO> page, String returnNo, Integer status, Integer userId) {
        return salesReturnMapper.selectReturnPage(page, returnNo, status, userId);
    }

    @Override
    public List<ReturnDetailVO> getDetails(Integer returnId) {
        return salesReturnDetailMapper.selectDetailsByReturnId(returnId);
    }

    /**
     * 恢复批次库存：追加到最近一批
     */
    private void restoreBatchStock(Goods goods, int count) {
        LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
        batchQuery.eq(GoodsBatch::getGoodsId, goods.getId())
                  .orderByDesc(GoodsBatch::getCreateTime).last("LIMIT 1");
        List<GoodsBatch> batches = goodsBatchMapper.selectList(batchQuery);
        if (batches != null && !batches.isEmpty()) {
            GoodsBatch latest = batches.get(0);
            latest.setCurrentCount(latest.getCurrentCount() + count);
            goodsBatchMapper.updateById(latest);
        }
    }

    private String generateReturnNo() {
        return "RET" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}
