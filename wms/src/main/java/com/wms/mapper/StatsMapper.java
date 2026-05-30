package com.wms.mapper;

import com.wms.entity.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 统计 Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@Mapper
public interface StatsMapper {

    /**
     * 获取今日销售统计汇总
     * @return 今日销售统计
     */
    TodaySummaryVO getTodaySummary();

    /**
     * 销售趋势统计（按天）
     * @param days 天数
     * @return 销售趋势列表
     */
    List<SalesTrendVO> getSalesTrend(@Param("days") Integer days);

    /**
     * 分类销售占比统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分类销售占比列表
     */
    List<CategoryRatioVO> getCategoryRatio(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 毛利统计（按天）
     * @param days 天数
     * @return 毛利统计列表
     */
    List<GrossProfitVO> getGrossProfit(@Param("days") Integer days);

    /**
     * 库存周转率分析
     * @return 周转率最高的前10名商品
     */
    List<TurnoverRateVO> getTopTurnoverRate();

    /**
     * 库存周转率分析（最低）
     * @return 周转率最低的前10名商品
     */
    List<TurnoverRateVO> getBottomTurnoverRate();

    /**
     * 智能补货建议
     * @param cycleDays 预计采购周期（天）
     * @return 需要补货的商品列表
     */
    List<RestockSuggestionVO> getRestockSuggestions(@Param("cycleDays") Integer cycleDays);

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
