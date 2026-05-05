package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.IStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 统计前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private IStatsService statsService;

    /**
     * 销售趋势统计（按天）
     * @param days 天数，默认7天
     * @return 销售趋势数据
     */
    @GetMapping("/salesTrend")
    public Result getSalesTrend(@RequestParam(required = false, defaultValue = "7") Integer days) {
        try {
            return Result.suc(statsService.getSalesTrend(days));
        } catch (Exception e) {
            return Result.fail("查询销售趋势失败：" + e.getMessage());
        }
    }

    /**
     * 分类销售占比统计
     * @param startDate 开始日期（格式：yyyy-MM-dd），默认最近7天
     * @param endDate 结束日期（格式：yyyy-MM-dd），默认为今天
     * @return 分类销售占比数据
     */
    @GetMapping("/categoryRatio")
    public Result getCategoryRatio(@RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate) {
        try {
            return Result.suc(statsService.getCategoryRatio(startDate, endDate));
        } catch (Exception e) {
            return Result.fail("查询分类占比失败：" + e.getMessage());
        }
    }

    /**
     * 毛利统计（按天）
     * @param days 天数，默认7天
     * @return 毛利统计数据
     */
    @GetMapping("/grossProfit")
    public Result getGrossProfit(@RequestParam(required = false, defaultValue = "7") Integer days) {
        try {
            return Result.suc(statsService.getGrossProfit(days));
        } catch (Exception e) {
            return Result.fail("查询毛利统计失败：" + e.getMessage());
        }
    }

    /**
     * 库存周转率分析（最高前10名）
     * @return 周转率最高的商品列表
     */
    @GetMapping("/turnoverRate/top")
    public Result getTopTurnoverRate() {
        try {
            return Result.suc(statsService.getTopTurnoverRate());
        } catch (Exception e) {
            return Result.fail("查询周转率失败：" + e.getMessage());
        }
    }

    /**
     * 库存周转率分析（最低前10名）
     * @return 周转率最低的商品列表
     */
    @GetMapping("/turnoverRate/bottom")
    public Result getBottomTurnoverRate() {
        try {
            return Result.suc(statsService.getBottomTurnoverRate());
        } catch (Exception e) {
            return Result.fail("查询周转率失败：" + e.getMessage());
        }
    }

    /**
     * 智能补货建议
     * @param cycleDays 预计采购周期（天），默认7天
     * @return 需要补货的商品列表
     */
    @GetMapping("/suggestRestock")
    public Result getSuggestRestock(@RequestParam(required = false, defaultValue = "7") Integer cycleDays) {
        try {
            return Result.suc(statsService.getRestockSuggestions(cycleDays));
        } catch (Exception e) {
            return Result.fail("查询补货建议失败：" + e.getMessage());
        }
    }

    /**
     * 库存预警列表
     * @return 库存低于预警值的商品列表
     */
    @GetMapping("/warningList")
    public Result getWarningList() {
        try {
            return Result.suc(statsService.getWarningList());
        } catch (Exception e) {
            return Result.fail("查询库存预警失败：" + e.getMessage());
        }
    }
}
