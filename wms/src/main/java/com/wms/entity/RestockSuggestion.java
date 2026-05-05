package com.wms.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 商品补货建议数据传输对象
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@Schema(name = "RestockSuggestion", description = "商品补货建议")
public class RestockSuggestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "当前库存")
    private Integer currentStock;

    @Schema(description = "近30天销量")
    private Integer recentSales;

    @Schema(description = "日均销量")
    private BigDecimal avgDailySales;

    @Schema(description = "建议采购量")
    private Integer suggestQuantity;

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

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getRecentSales() {
        return recentSales;
    }

    public void setRecentSales(Integer recentSales) {
        this.recentSales = recentSales;
    }

    public BigDecimal getAvgDailySales() {
        return avgDailySales;
    }

    public void setAvgDailySales(BigDecimal avgDailySales) {
        this.avgDailySales = avgDailySales;
    }

    public Integer getSuggestQuantity() {
        return suggestQuantity;
    }

    public void setSuggestQuantity(Integer suggestQuantity) {
        this.suggestQuantity = suggestQuantity;
    }

    @Override
    public String toString() {
        return "RestockSuggestion{" +
            "goodsId=" + goodsId +
            ", goodsName='" + goodsName + '\'' +
            ", currentStock=" + currentStock +
            ", recentSales=" + recentSales +
            ", avgDailySales=" + avgDailySales +
            ", suggestQuantity=" + suggestQuantity +
            '}';
    }
}
