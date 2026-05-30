package com.wms.service;

import com.wms.entity.*;

import java.util.List;

/**
 * <p>
 * 统计服务类
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
public interface IStatsService {

    /**
     * 获取今日销售统计汇总
     * @return 今日销售统计
     */
    TodaySummaryVO getTodaySummary();

    /**
     * 销售趋势统计（按天）
     * @param days 天数，默认7天
     * @return 销售趋势列表
     */
    List<SalesTrendVO> getSalesTrend(Integer days);

    /**
     * 分类销售占比统计
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 分类销售占比列表
     */
    List<CategoryRatioVO> getCategoryRatio(String startDate, String endDate);

    /**
     * 毛利统计（按天）
     * @param days 天数，默认7天
     * @return 毛利统计列表
     */
    List<GrossProfitVO> getGrossProfit(Integer days);

    /**
     * 库存周转率分析（最高前10名）
     * @return 周转率最高的商品列表
     */
    List<TurnoverRateVO> getTopTurnoverRate();

    /**
     * 库存周转率分析（最低前10名）
     * @return 周转率最低的商品列表
     */
    List<TurnoverRateVO> getBottomTurnoverRate();

    /**
     * @param cycleDays 预计采购周期（天），默认7天
     * @return 需要补货的商品列表
     */
    List<RestockSuggestionVO> getRestockSuggestions(Integer cycleDays);

    /**
     * 库存预警列表
     * @return 库存低于预警值的商品列表
     */
    List<Goods> getWarningList();

    /**
     * 按商品销售统计（近30天）
     */
    List<ProductSalesVO> getProductSales();
}
