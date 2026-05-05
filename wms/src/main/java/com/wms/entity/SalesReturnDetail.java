package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退货明细实体类
 */
@Schema(name = "SalesReturnDetail", description = "退货明细")
public class SalesReturnDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "退货单ID")
    @TableField("return_id")
    private Integer returnId;

    @Schema(description = "商品ID")
    @TableField("goods_id")
    private Integer goodsId;

    @Schema(description = "退货数量")
    @TableField("return_count")
    private Integer returnCount;

    @Schema(description = "单价")
    @TableField("refund_amount")
    private BigDecimal price;

    @Schema(description = "小计金额")
    @TableField("subtotal")
    private BigDecimal subtotal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getReturnId() {
        return returnId;
    }

    public void setReturnId(Integer returnId) {
        this.returnId = returnId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getReturnCount() {
        return returnCount;
    }

    public void setReturnCount(Integer returnCount) {
        this.returnCount = returnCount;
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

    @Override
    public String toString() {
        return "SalesReturnDetail{" +
                "id=" + id +
                ", returnId=" + returnId +
                ", goodsId=" + goodsId +
                ", returnCount=" + returnCount +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
}
