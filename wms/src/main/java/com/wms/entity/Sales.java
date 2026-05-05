package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 销售单实体类
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@Schema(name = "Sales", description = "销售单")
public class Sales implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Integer userId;

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

    @Override
    public String toString() {
        return "Sales{" +
            "id = " + id +
            ", userId = " + userId +
            ", totalAmount = " + totalAmount +
            ", realAmount = " + realAmount +
            ", changeAmount = " + changeAmount +
            ", orderNum = " + orderNum +
            ", createTime = " + createTime +
        "}";
    }
}
