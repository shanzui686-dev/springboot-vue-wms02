package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 采购明细表
 * </p>
 *
 * @author wms
 * @since 2026-04-20
 */
@Schema(name = "PurchaseDetail", description = "采购明细表")
public class PurchaseDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "采购单ID")
    private Integer purchaseId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "数量")
    private Integer count;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "小计金额")
    private BigDecimal subtotal;

    @Schema(description = "备注")
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Integer purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "PurchaseDetail{" +
            "id = " + id +
            ", purchaseId = " + purchaseId +
            ", goodsId = " + goodsId +
            ", count = " + count +
            ", price = " + price +
            ", subtotal = " + subtotal +
            ", remark = " + remark +
        "}";
    }
}
