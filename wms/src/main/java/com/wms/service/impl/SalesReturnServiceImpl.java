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
import com.wms.service.ISalesDetailService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesReturnServiceImpl extends ServiceImpl<SalesReturnMapper, SalesReturn> implements ISalesReturnService {

    @Autowired
    private ISalesService salesService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISalesReturnDetailService salesReturnDetailService;

    @Autowired
    private ISalesDetailService salesDetailService;

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
        if (sales.getStatus() != null && sales.getStatus() == 1) {
            throw new RuntimeException("该销售单已退款，不能重复退货");
        }

        // 数量防超退：剩余可退 = 销售数量 - 未拒绝退货单（状态0/1）已申请数量
        Map<Integer, Integer> soldQtyMap = getSoldQtyMap(returnApplyDTO.getSalesId());
        Map<Integer, Integer> returnedQtyMap = getReturnedQtyMap(returnApplyDTO.getSalesId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnApplyDTO.ReturnItemDTO item : returnApplyDTO.getItems()) {
            Goods goods = goodsService.getById(item.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + item.getGoodsId() + "】不存在");
            }
            if (item.getReturnCount() == null || item.getReturnCount() <= 0) {
                throw new RuntimeException("商品【" + goods.getName() + "】退货数量必须大于0");
            }
            int remaining = soldQtyMap.getOrDefault(item.getGoodsId(), 0)
                    - returnedQtyMap.getOrDefault(item.getGoodsId(), 0);
            if (item.getReturnCount() > remaining) {
                throw new RuntimeException("商品【" + goods.getName() + "】可退数量仅剩" + Math.max(remaining, 0) + "件");
            }
            BigDecimal subtotal = goods.getRetailPrice()
                    .multiply(BigDecimal.valueOf(item.getReturnCount()));
            totalAmount = totalAmount.add(subtotal);
        }

        String returnNo = sales.getOrderNum() != null
                ? "TH" + sales.getOrderNum()
                : "TH" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        int type = returnApplyDTO.getType() != null ? returnApplyDTO.getType() : 1;

        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnNum(returnNo);
        salesReturn.setReturnNo(returnNo);
        salesReturn.setSalesId(returnApplyDTO.getSalesId());
        salesReturn.setReturnReason(returnApplyDTO.getReturnReason());
        salesReturn.setReturnAmount(totalAmount);
        // 换货(2)即时执行无需审核，落库即已完成；退货(1)进入待退款
        salesReturn.setStatus(type == 2 ? 1 : 0);
        salesReturn.setCreateTime(LocalDateTime.now());
        salesReturn.setIsResalable(returnApplyDTO.getIsResalable() != null ? returnApplyDTO.getIsResalable() : 0);
        salesReturn.setType(type);

        if (!this.save(salesReturn)) {
            throw new RuntimeException("退货单保存失败");
        }

        Integer returnId = salesReturn.getId();

        List<SalesReturnDetail> detailList = new ArrayList<>();
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

        // 换货：提交即执行商品互换（无申请、无退款、无状态流转，不改销售单状态）
        if (type == 2) {
            processReturnGoods(salesReturn, detailList);
            processExchangeGoods(salesReturn, detailList);
            for (SalesReturnDetail detail : detailList) {
                markSalesDetailStatus(salesReturn.getSalesId(), detail.getGoodsId(), 2);
            }
        }

        return returnId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyWholeOrder(String orderNum) {
        if (orderNum == null || orderNum.trim().isEmpty()) {
            throw new RuntimeException("请输入销售单流水号");
        }
        Sales sales = salesService.getOne(new LambdaQueryWrapper<Sales>()
                .eq(Sales::getOrderNum, orderNum.trim()));
        if (sales == null) {
            throw new RuntimeException("销售单【" + orderNum + "】不存在");
        }

        // 整单申请只提交剩余可退部分（已拒绝的退货单不占用额度）
        Map<Integer, Integer> soldQtyMap = getSoldQtyMap(sales.getId());
        Map<Integer, Integer> returnedQtyMap = getReturnedQtyMap(sales.getId());

        List<ReturnApplyDTO.ReturnItemDTO> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : soldQtyMap.entrySet()) {
            int remaining = entry.getValue() - returnedQtyMap.getOrDefault(entry.getKey(), 0);
            if (remaining > 0) {
                ReturnApplyDTO.ReturnItemDTO item = new ReturnApplyDTO.ReturnItemDTO();
                item.setGoodsId(entry.getKey());
                item.setReturnCount(remaining);
                items.add(item);
            }
        }
        if (items.isEmpty()) {
            throw new RuntimeException("该销售单无可退商品");
        }

        ReturnApplyDTO dto = new ReturnApplyDTO();
        dto.setSalesId(sales.getId());
        dto.setType(1);
        dto.setReturnReason("收银台退货退款申请");
        dto.setItems(items);
        return applyReturn(dto);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmRefund(Integer returnId) {
        SalesReturn salesReturn = this.getById(returnId);
        if (salesReturn == null) {
            throw new RuntimeException("退货单不存在");
        }
        if (salesReturn.getType() != null && salesReturn.getType() == 2) {
            throw new RuntimeException("换货单无需确认退款");
        }
        if (salesReturn.getStatus() != null && salesReturn.getStatus() == 1) {
            throw new RuntimeException("该退货单已退款，无需重复操作");
        }
        if (salesReturn.getStatus() != null && salesReturn.getStatus() == 2) {
            throw new RuntimeException("该退货单已拒绝，不能确认退款");
        }

        List<SalesReturnDetail> detailList = getReturnDetails(returnId);
        if (detailList == null || detailList.isEmpty()) {
            throw new RuntimeException("退货明细不存在");
        }

        processReturnGoods(salesReturn, detailList);

        // 联动销售明细/主表状态
        for (SalesReturnDetail detail : detailList) {
            markSalesDetailStatus(salesReturn.getSalesId(), detail.getGoodsId(), 1);
        }
        updateSalesStatusIfAllDone(salesReturn.getSalesId());

        salesReturn.setStatus(1);
        salesReturn.setRefundTime(LocalDateTime.now());
        return this.updateById(salesReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectRefund(Integer returnId) {
        SalesReturn salesReturn = this.getById(returnId);
        if (salesReturn == null) {
            throw new RuntimeException("退货单不存在");
        }
        if (salesReturn.getType() != null && salesReturn.getType() == 2) {
            throw new RuntimeException("换货单无需审核");
        }
        if (salesReturn.getStatus() != null && salesReturn.getStatus() == 1) {
            throw new RuntimeException("该退货单已退款，不能拒绝");
        }
        if (salesReturn.getStatus() != null && salesReturn.getStatus() == 2) {
            throw new RuntimeException("该退货单已拒绝，无需重复操作");
        }

        salesReturn.setStatus(2); // 已拒绝：不退款、不回滚库存
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
     * 退回商品处理：可二次销售→恢复批次库存；不可二次销售→生成损耗单。均写操作日志
     */
    private void processReturnGoods(SalesReturn salesReturn, List<SalesReturnDetail> detailList) {
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
    }

    /**
     * 换货处理：扣减换货目标商品库存（FIFO）
     */
    private void processExchangeGoods(SalesReturn salesReturn, List<SalesReturnDetail> detailList) {
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

    private List<SalesReturnDetail> getReturnDetails(Integer returnId) {
        LambdaQueryWrapper<SalesReturnDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SalesReturnDetail::getReturnId, returnId);
        return salesReturnDetailService.list(queryWrapper);
    }

    /**
     * 统计销售单各商品销售数量
     */
    private Map<Integer, Integer> getSoldQtyMap(Integer salesId) {
        LambdaQueryWrapper<SalesDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SalesDetail::getSalesId, salesId);
        List<SalesDetail> details = salesDetailService.list(queryWrapper);

        Map<Integer, Integer> map = new HashMap<>();
        if (details != null) {
            for (SalesDetail detail : details) {
                map.merge(detail.getGoodsId(), detail.getCount() == null ? 0 : detail.getCount(), Integer::sum);
            }
        }
        return map;
    }

    /**
     * 统计销售单各商品已申请退货数量（未拒绝的退货单：状态0/1，已拒绝不占用额度）
     */
    private Map<Integer, Integer> getReturnedQtyMap(Integer salesId) {
        LambdaQueryWrapper<SalesReturn> returnQuery = new LambdaQueryWrapper<>();
        returnQuery.eq(SalesReturn::getSalesId, salesId)
                   .in(SalesReturn::getStatus, 0, 1);
        List<SalesReturn> returns = this.list(returnQuery);

        Map<Integer, Integer> map = new HashMap<>();
        if (returns == null || returns.isEmpty()) {
            return map;
        }

        List<Integer> returnIds = new ArrayList<>();
        for (SalesReturn salesReturn : returns) {
            returnIds.add(salesReturn.getId());
        }

        LambdaQueryWrapper<SalesReturnDetail> detailQuery = new LambdaQueryWrapper<>();
        detailQuery.in(SalesReturnDetail::getReturnId, returnIds);
        List<SalesReturnDetail> details = salesReturnDetailService.list(detailQuery);
        if (details != null) {
            for (SalesReturnDetail detail : details) {
                map.merge(detail.getGoodsId(), detail.getReturnCount() == null ? 0 : detail.getReturnCount(), Integer::sum);
            }
        }
        return map;
    }

    /**
     * 更新销售明细状态（1退货/2换货）
     */
    private void markSalesDetailStatus(Integer salesId, Integer goodsId, int status) {
        LambdaQueryWrapper<SalesDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SalesDetail::getSalesId, salesId)
                    .eq(SalesDetail::getGoodsId, goodsId);
        List<SalesDetail> details = salesDetailService.list(queryWrapper);
        if (details != null) {
            for (SalesDetail detail : details) {
                detail.setStatus(status);
                salesDetailService.updateById(detail);
            }
        }
    }

    /**
     * 该销售单全部明细均已退货/换货时，主表标记为已退款
     */
    private void updateSalesStatusIfAllDone(Integer salesId) {
        LambdaQueryWrapper<SalesDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SalesDetail::getSalesId, salesId);
        List<SalesDetail> details = salesDetailService.list(queryWrapper);
        if (details == null || details.isEmpty()) {
            return;
        }
        for (SalesDetail detail : details) {
            if (detail.getStatus() == null || detail.getStatus() == 0) {
                return;
            }
        }
        Sales sales = salesService.getById(salesId);
        if (sales != null && (sales.getStatus() == null || sales.getStatus() == 0)) {
            sales.setStatus(1); // 已退款
            salesService.updateById(sales);
        }
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
