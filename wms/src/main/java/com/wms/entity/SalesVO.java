package com.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售单查询结果VO（含收银员姓名）
 */
@Schema(name = "SalesVO", description = "销售单查询结果")
public class SalesVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "收银员姓名")
    private String cashierName;

    @Schema(description = "应收金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "实收金额")
    @TableField("real_amount")
    private BigDecimal realAmount;

    @Schema(description = "找零金额")
    @TableField("change_amount")
    private BigDecimal changeAmount;

    @Schema(description = "订单流水号")
    @TableField("order_num")
    private String orderNum;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "退货状态：0未退货，1待退款，2已退款")
    private Integer returnStatus;

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

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
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

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(Integer returnStatus) {
        this.returnStatus = returnStatus;
    }

    @Override
    public String toString() {
        return "SalesVO{" +
                "id=" + id +
                ", userId=" + userId +
                ", cashierName='" + cashierName + '\'' +
                ", totalAmount=" + totalAmount +
                ", realAmount=" + realAmount +
                ", changeAmount=" + changeAmount +
                ", orderNum='" + orderNum + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
