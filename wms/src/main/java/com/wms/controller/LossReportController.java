package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.RequireRole;
import com.wms.common.Result;
import com.wms.entity.LossReport;
import com.wms.entity.LossReportVO;
import com.wms.service.ILossReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/lossReport")
@RequireRole({0, 1, 2}) // 所有角色都可上报/查看损耗
public class LossReportController {

    @Autowired
    private ILossReportService lossReportService;

    /**
     * 损耗上报（库管员/收银员）
     */
    @Log("损耗上报")
    @PostMapping("/report")
    public Result report(@RequestBody LossReport lossReport) {
        try {
            return lossReportService.reportLoss(lossReport) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核通过（店长）
     */
    @Log("损耗审核通过")
    @PostMapping("/audit")
    public Result audit(@RequestParam Integer lossId, @RequestParam Integer auditorId) {
        try {
            return lossReportService.auditLoss(lossId, auditorId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核拒绝（店长）
     */
    @Log("损耗审核拒绝")
    @PostMapping("/reject")
    public Result reject(@RequestParam Integer lossId, @RequestParam Integer auditorId) {
        try {
            return lossReportService.rejectLoss(lossId, auditorId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 分页查询损耗单
     */
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();

        String lossNo = (String) param.get("lossNo");
        Integer status = null;
        Integer lossType = null;
        if (param.get("status") != null) {
            status = Integer.parseInt(param.get("status").toString());
        }
        if (param.get("lossType") != null) {
            lossType = Integer.parseInt(param.get("lossType").toString());
        }

        Integer reporterId = null;
        if (param.get("reporterId") != null) {
            reporterId = Integer.parseInt(param.get("reporterId").toString());
        }

        IPage<LossReportVO> result = lossReportService.listPage(
                query.getPagenum(), query.getPagesize(), lossNo, status, lossType, reporterId);

        return Result.suc(result.getRecords(), result.getTotal());
    }

    /**
     * 查询损耗单详情
     */
    @GetMapping("/detail")
    public Result getDetail(@RequestParam Integer lossId) {
        LossReport lossReport = lossReportService.getById(lossId);
        return lossReport != null ? Result.suc(lossReport) : Result.fail("损耗单不存在");
    }

    /**
     * 查询所有待审核的损耗单
     */
    @GetMapping("/pending")
    public Result getPending() {
        LambdaQueryWrapper<LossReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LossReport::getStatus, 0).orderByDesc(LossReport::getCreateTime);
        List<LossReport> list = lossReportService.list(queryWrapper);
        return Result.suc(list);
    }
}
