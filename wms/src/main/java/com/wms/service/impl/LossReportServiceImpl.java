package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.*;
import com.wms.mapper.GoodsBatchMapper;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.LossReportMapper;
import com.wms.service.ILossReportService;
import com.wms.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class LossReportServiceImpl extends ServiceImpl<LossReportMapper, LossReport> implements ILossReportService {

    @Autowired
    private LossReportMapper lossReportMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;

    @Autowired
    private IRecordService recordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reportLoss(LossReport lossReport) {
        // 1. 生成损耗单号：LOSS + 年月日 + 6位随机数
        String lossNo = "LOSS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + (int)(Math.random() * 900000 + 100000);
        lossReport.setLossNo(lossNo);
        lossReport.setStatus(0);
        lossReport.setCreateTime(LocalDateTime.now());

        // 2. 快照进价（从 goods_batch 获取）
        if (lossReport.getBatchId() != null) {
            GoodsBatch batch = goodsBatchMapper.selectById(lossReport.getBatchId());
            if (batch != null) {
                lossReport.setPurchasePrice(batch.getPurchasePrice());
                lossReport.setBatchNo(batch.getBatchNo());
            }
        }

        // 3. 计算损耗金额
        if (lossReport.getPurchasePrice() != null && lossReport.getLossCount() != null) {
            BigDecimal amount = lossReport.getPurchasePrice()
                    .multiply(BigDecimal.valueOf(lossReport.getLossCount()))
                    .setScale(2, RoundingMode.HALF_UP);
            lossReport.setLossAmount(amount);
        }

        return this.save(lossReport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditLoss(Integer lossId, Integer auditorId) {
        LossReport lossReport = this.getById(lossId);
        if (lossReport == null) {
            throw new RuntimeException("损耗单不存在");
        }
        if (lossReport.getStatus() != 0) {
            throw new RuntimeException("损耗单状态不正确，无法审核");
        }

        // 库存损耗：扣减批次库存 + 商品总库存
        if (lossReport.getLossType() == 1) {
            deductBatchStock(lossReport);
        }
        // 销售损耗：仅记录损耗金额，不重复扣库存（退换货时已处理）

        // 插入流水记录
        Record record = new Record();
        record.setGoods(lossReport.getGoodsId());
        record.setUserId(null);
        record.setAdminId(auditorId);
        record.setCount(-lossReport.getLossCount());
        record.setCreatetime(LocalDateTime.now());
        record.setOperationType(lossReport.getLossType() == 1 ? "库存损耗" : "销售损耗");
        record.setRefOrderNum(lossReport.getLossNo());
        record.setRemark("损耗审批通过，批次:" + lossReport.getBatchNo()
                + "，数量:" + lossReport.getLossCount()
                + "，损耗金额:" + lossReport.getLossAmount()
                + "，原因:" + (lossReport.getReason() != null ? lossReport.getReason() : "无"));
        record.setStatus(1);
        recordService.save(record);

        lossReport.setStatus(1);
        lossReport.setAuditorId(auditorId);
        lossReport.setAuditTime(LocalDateTime.now());
        return this.updateById(lossReport);
    }

    @Override
    public boolean rejectLoss(Integer lossId, Integer auditorId) {
        LossReport lossReport = this.getById(lossId);
        if (lossReport == null) {
            throw new RuntimeException("损耗单不存在");
        }
        if (lossReport.getStatus() != 0) {
            throw new RuntimeException("损耗单状态不正确，无法审核");
        }

        lossReport.setStatus(2);
        lossReport.setAuditorId(auditorId);
        lossReport.setAuditTime(LocalDateTime.now());
        return this.updateById(lossReport);
    }

    @Override
    public IPage<LossReportVO> listPage(Integer pageNum, Integer pageSize,
                                        String lossNo, Integer status, Integer lossType, Integer reporterId) {
        Page<LossReportVO> page = new Page<>(pageNum, pageSize);
        return lossReportMapper.selectLossPage(page, lossNo, status, lossType, reporterId);
    }

    /**
     * 库存损耗 — 扣减批次库存（FIFO） + 商品总库存
     */
    private void deductBatchStock(LossReport lossReport) {
        int remaining = lossReport.getLossCount();

        // 优先从指定批次扣减
        if (lossReport.getBatchId() != null) {
            GoodsBatch batch = goodsBatchMapper.selectById(lossReport.getBatchId());
            if (batch != null) {
                if (batch.getCurrentCount() < remaining) {
                    throw new RuntimeException("批次" + batch.getBatchNo() + "库存不足，当前剩余:" + batch.getCurrentCount() + "，损耗:" + remaining);
                }
                batch.setCurrentCount(batch.getCurrentCount() - remaining);
                goodsBatchMapper.updateById(batch);
                remaining = 0;
            }
        }

        // 如果指定批次不够，按FIFO逐批扣减
        if (remaining > 0) {
            LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
            batchQuery.eq(GoodsBatch::getGoodsId, lossReport.getGoodsId())
                      .gt(GoodsBatch::getCurrentCount, 0)
                      .orderByAsc(GoodsBatch::getCreateTime);
            for (GoodsBatch batch : goodsBatchMapper.selectList(batchQuery)) {
                if (remaining <= 0) break;
                int deduct = Math.min(batch.getCurrentCount(), remaining);
                batch.setCurrentCount(batch.getCurrentCount() - deduct);
                goodsBatchMapper.updateById(batch);
                remaining -= deduct;
            }
            if (remaining > 0) {
                throw new RuntimeException("所有批次库存不足，缺少:" + remaining);
            }
        }

        // 扣减商品总库存
        Goods goods = goodsMapper.selectById(lossReport.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        int newCount = goods.getCount() - lossReport.getLossCount();
        if (newCount < 0) {
            throw new RuntimeException("商品库存不足，当前:" + goods.getCount() + "，损耗:" + lossReport.getLossCount());
        }
        goods.setCount(newCount);
        goodsMapper.updateById(goods);
    }
}
