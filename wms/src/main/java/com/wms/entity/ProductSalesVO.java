package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "按商品销售统计")
public class ProductSalesVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer goodsId;
    private String goodsName;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal totalProfit;
    /** 退换货类型 1=退货 2=换货（仅退换货商品汇总使用） */
    private Integer type;

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalProfit() { return totalProfit; }
    public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }
}
