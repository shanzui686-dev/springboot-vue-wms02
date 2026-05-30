package com.wms.service.impl;

import com.wms.entity.CategoryRatioVO;
import com.wms.entity.Goods;
import com.wms.entity.GrossProfitVO;
import com.wms.entity.ProductSalesVO;
import com.wms.entity.RestockSuggestionVO;
import com.wms.entity.SalesTrendVO;
import com.wms.entity.TodaySummaryVO;
import com.wms.entity.TurnoverRateVO;
import com.wms.mapper.StatsMapper;
import com.wms.service.IStatsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 统计服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@Service
public class StatsServiceImpl implements IStatsService {

    @Autowired
    private StatsMapper statsMapper;

    /**
     * 获取今日销售统计汇总
     * @return 今日销售统计
     */
    @Override
    public TodaySummaryVO getTodaySummary() {
        return statsMapper.getTodaySummary();
    }

    /**
     * 销售趋势统计（按天）
     * @param days 天数，默认7天
     * @return 销售趋势列表
     */
    @Override
    public List<SalesTrendVO> getSalesTrend(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        return statsMapper.getSalesTrend(days);
    }

    /**
     * 分类销售占比统计
     * @param startDate 开始日期（格式：yyyy-MM-dd）
     * @param endDate 结束日期（格式：yyyy-MM-dd）
     * @return 分类销售占比列表
     */
    @Override
    public List<CategoryRatioVO> getCategoryRatio(String startDate, String endDate) {
        // 参数校验和默认值处理
        if (StringUtils.isBlank(startDate)) {
            // 默认查询最近7天
            startDate = LocalDate.now().minusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (StringUtils.isBlank(endDate)) {
            // 默认为明天（包含当天所有数据）
            endDate = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        
        return statsMapper.getCategoryRatio(startDate, endDate);
    }

    /**
     * 毛利统计（按天）
     * @param days 天数，默认7天
     * @return 毛利统计列表
     */
    @Override
    public List<GrossProfitVO> getGrossProfit(Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        return statsMapper.getGrossProfit(days);
    }

    /**
     * 库存周转率分析（最高前10名）
     * @return 周转率最高的商品列表
     */
    @Override
    public List<TurnoverRateVO> getTopTurnoverRate() {
        return statsMapper.getTopTurnoverRate();
    }

    /**
     * 库存周转率分析（最低前10名）
     * @return 周转率最低的商品列表
     */
    @Override
    public List<TurnoverRateVO> getBottomTurnoverRate() {
        return statsMapper.getBottomTurnoverRate();
    }

    /**
     * 智能补货建议
     * @param cycleDays 预计采购周期（天），默认7天
     * @return 需要补货的商品列表
     */
    @Override
    public List<RestockSuggestionVO> getRestockSuggestions(Integer cycleDays) {
        if (cycleDays == null || cycleDays <= 0) {
            cycleDays = 7;
        }
        return statsMapper.getRestockSuggestions(cycleDays);
    }

    /**
     * 库存预警列表
     * @return 库存低于预警值的商品列表
     */
    @Override
    public List<Goods> getWarningList() {
        return statsMapper.getWarningList();
    }

    @Override
    public List<ProductSalesVO> getProductSales() {
        return statsMapper.getProductSales();
    }
}
