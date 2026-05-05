package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 销售单数据传输对象（用于收银台）
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@Schema(name = "SalesDTO", description = "销售单数据传输对象")
public class SalesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "应收金额")
    private BigDecimal totalAmount;

    @Schema(description = "实收金额")
    private BigDecimal realAmount;

    @Schema(description = "找零金额")
    private BigDecimal changeAmount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "销售明细列表")
    private List<SalesDetail> details;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<SalesDetail> getDetails() {
        return details;
    }

    public void setDetails(List<SalesDetail> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "SalesDTO{" +
            "id = " + id +
            ", userId = " + userId +
            ", totalAmount = " + totalAmount +
            ", realAmount = " + realAmount +
            ", changeAmount = " + changeAmount +
            ", createTime = " + createTime +
            ", details = " + details +
        "}";
    }
}
