package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.LossReport;
import com.wms.entity.LossReportVO;

public interface ILossReportService extends IService<LossReport> {

    /**
     * 损耗上报（生成LOSS单号 → 插入主表 → status=0待审核）
     */
    boolean reportLoss(LossReport lossReport);

    /**
     * 审核通过（扣减批次库存+商品库存 → 记录流水 → 记录损耗金额 → status=1已处理）
     */
    boolean auditLoss(Integer lossId, Integer auditorId);

    /**
     * 审核拒绝（status=2已拒绝）
     */
    boolean rejectLoss(Integer lossId, Integer auditorId);

    /**
     * 分页查询损耗单（连表查商品名/上报人/审核人）
     */
    IPage<LossReportVO> listPage(Integer pageNum, Integer pageSize,
                                 String lossNo, Integer status, Integer lossType, Integer reporterId);
}
