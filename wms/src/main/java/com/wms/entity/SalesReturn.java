package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退货单实体类
 */
@Schema(name = "SalesReturn", description = "退货单")
public class SalesReturn implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "退货单号")
    @TableField(value = "order_num", insertStrategy = com.baomidou.mybatisplus.annotation.FieldStrategy.NEVER)
    private String returnNo;

    @Schema(description = "原销售单ID")
    @TableField("sales_id")
    private Integer salesId;

    @Schema(description = "退货单流水号")
    @TableField("return_num")
    private String returnNum;

    @Schema(description = "退货原因")
    @TableField("remark")
    private String returnReason;

    @Schema(description = "退货总金额")
    @TableField("total_refund")
    private BigDecimal returnAmount;

    @Schema(description = "状态：0待退款，1已退款")
    @TableField("status")
    private Integer status;

    @Schema(description = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @Schema(description = "退款时间")
    @TableField("refund_time")
    private LocalDateTime refundTime;

    @Schema(description = "操作收银员ID")
    @TableField("user_id")
    private Integer userId;

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

    public Integer getSalesId() {
        return salesId;
    }

    public void setSalesId(Integer salesId) {
        this.salesId = salesId;
    }

    public String getReturnNum() {
        return returnNum;
    }

    public void setReturnNum(String returnNum) {
        this.returnNum = returnNum;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "SalesReturn{" +
                "id=" + id +
                ", returnNo='" + returnNo + '\'' +
                ", salesId=" + salesId +
                ", returnReason='" + returnReason + '\'' +
                ", returnAmount=" + returnAmount +
                ", status=" + status +
                ", createTime=" + createTime +
                ", refundTime=" + refundTime +
                '}';
    }
}
