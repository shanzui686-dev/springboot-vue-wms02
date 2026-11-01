package com.wms.entity;

import java.math.BigDecimal;

/**
 * 销售汇总 VO（销售笔数 / 应收总额 / 实收总额）
 */
public class SalesSummaryVO {

    /** 销售笔数 */
    private Long count;

    /** 应收总额 */
    private BigDecimal totalAmount;

    /** 实收总额 */
    private BigDecimal realAmount;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(BigDecimal realAmount) {
        this.realAmount = realAmount;
    }
}
