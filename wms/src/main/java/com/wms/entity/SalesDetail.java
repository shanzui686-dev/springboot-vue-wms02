package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 销售明细实体类
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@Schema(name = "SalesDetail", description = "销售明细")
public class SalesDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "销售单ID")
    @TableField("sales_id")
    private Integer salesId;

    @Schema(description = "商品ID")
    @TableField("goods_id")
    private Integer goodsId;

    @Schema(description = "数量")
    @TableField("count")
    private Integer count;

    @Schema(description = "单价")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "小计")
    @TableField("subtotal")
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
        return "SalesDetail{" +
            "id = " + id +
            ", salesId = " + salesId +
            ", goodsId = " + goodsId +
            ", count = " + count +
            ", price = " + price +
            ", subtotal = " + subtotal +
        "}";
    }
}
