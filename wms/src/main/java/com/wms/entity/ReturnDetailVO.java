package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 退货明细查询VO
 */
@Schema(name = "ReturnDetailVO", description = "退货明细查询结果")
public class ReturnDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "退货单ID")
    private Integer returnId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "退货数量")
    private Integer returnCount;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "小计金额")
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

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
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
        return "ReturnDetailVO{" +
                "id=" + id +
                ", returnId=" + returnId +
                ", goodsId=" + goodsId +
                ", goodsName='" + goodsName + '\'' +
                ", returnCount=" + returnCount +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
}
