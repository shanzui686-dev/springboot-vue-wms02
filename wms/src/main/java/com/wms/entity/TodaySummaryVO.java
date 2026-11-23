package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 今日销售统计汇总 VO
 */
@Schema(description = "今日销售统计汇总")
public class TodaySummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "今日销售额")
    private BigDecimal todaySales;

    @Schema(description = "今日毛利")
    private BigDecimal todayProfit;

    @Schema(description = "今日订单数")
    private Integer todayOrders;

    @Schema(description = "今日损耗金额")
    private BigDecimal todayLoss;

    @Schema(description = "净利润(毛利-损耗)")
    private BigDecimal netProfit;

    public BigDecimal getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(BigDecimal todaySales) {
        this.todaySales = todaySales;
    }

    public BigDecimal getTodayProfit() {
        return todayProfit;
    }

    public void setTodayProfit(BigDecimal todayProfit) {
        this.todayProfit = todayProfit;
    }

    public Integer getTodayOrders() {
        return todayOrders;
    }

    public void setTodayOrders(Integer todayOrders) { this.todayOrders = todayOrders; }

    public BigDecimal getTodayLoss() { return todayLoss; }
    public void setTodayLoss(BigDecimal todayLoss) { this.todayLoss = todayLoss; }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
}
