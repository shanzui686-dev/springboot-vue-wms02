package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 销售明细查询结果VO（含商品名称）
 */
@Schema(name = "SalesDetailVO", description = "销售明细查询结果")
public class SalesDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "销售单ID")
    private Integer salesId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "数量")
    private Integer count;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSalesId() {
        return salesId;
    }

    public void setSalesId(Integer salesId) {
        this.salesId = salesId;
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

    @Override
    public String toString() {
        return "SalesDetailVO{" +
                "id=" + id +
                ", salesId=" + salesId +
                ", goodsId=" + goodsId +
                ", goodsName='" + goodsName + '\'' +
                ", count=" + count +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
}
