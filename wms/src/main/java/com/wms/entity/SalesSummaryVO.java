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

    /** 退换货笔数（有退换货记录的销售单数） */
    private Long returnCount;

    /** 已退货笔数 */
    private Long returnDoneCount;

    /** 待退款笔数 */
    private Long returnPendingCount;

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

    public Long getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(Long returnCount) {
        this.returnCount = returnCount;
    }

    public Long getReturnDoneCount() {
        return returnDoneCount;
    }

    public void setReturnDoneCount(Long returnDoneCount) {
        this.returnDoneCount = returnDoneCount;
    }

    public Long getReturnPendingCount() {
        return returnPendingCount;
    }

    public void setReturnPendingCount(Long returnPendingCount) {
        this.returnPendingCount = returnPendingCount;
    }
}
