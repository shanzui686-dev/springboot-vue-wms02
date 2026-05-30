package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货单查询VO
 */
@Schema(name = "SalesReturnVO", description = "退货单查询结果")
public class SalesReturnVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "退货单号")
    private String returnNo;

    @Schema(description = "退货单流水号")
    private String returnNum;

    @Schema(description = "原销售单ID")
    private Integer salesId;

    @Schema(description = "原销售流水号")
    private String salesOrderNum;

    @Schema(description = "收银员姓名")
    private String cashierName;

    @Schema(description = "退货原因")
    private String returnReason;

    @Schema(description = "退货总金额")
    private BigDecimal returnAmount;

    @Schema(description = "状态：0待退款，1已退款")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "退款时间")
    private LocalDateTime refundTime;

    @Schema(description = "是否影响二次销售")
    private Integer isResalable;

    @Schema(description = "类型(1退货/2换货)")
    private Integer type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReturnNo() {
        return returnNo;
    }

    public void setReturnNo(String returnNo) {
        this.returnNo = returnNo;
    }

    public String getReturnNum() {
        return returnNum;
    }

    public void setReturnNum(String returnNum) {
        this.returnNum = returnNum;
    }

    public Integer getSalesId() {
        return salesId;
    }

    public void setSalesId(Integer salesId) {
        this.salesId = salesId;
    }

    public String getSalesOrderNum() {
        return salesOrderNum;
    }

    public void setSalesOrderNum(String salesOrderNum) {
        this.salesOrderNum = salesOrderNum;
    }

    public String getCashierName() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName = cashierName;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public BigDecimal getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(BigDecimal returnAmount) {
        this.returnAmount = returnAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public Integer getIsResalable() { return isResalable; }
    public void setIsResalable(Integer isResalable) { this.isResalable = isResalable; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    @Override
    public String toString() {
        return "SalesReturnVO{" +
                "id=" + id +
                ", returnNo='" + returnNo + '\'' +
                ", salesId=" + salesId +
                ", salesOrderNum='" + salesOrderNum + '\'' +
                ", cashierName='" + cashierName + '\'' +
                ", returnReason='" + returnReason + '\'' +
                ", returnAmount=" + returnAmount +
                ", status=" + status +
                ", statusText='" + statusText + '\'' +
                ", createTime=" + createTime +
                ", refundTime=" + refundTime +
                '}';
    }
}
