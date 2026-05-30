package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 退货申请DTO
 */
@Schema(name = "ReturnApplyDTO", description = "退货申请数据传输对象")
public class ReturnApplyDTO {

    @Schema(description = "原销售单ID")
    private Integer salesId;

    @Schema(description = "退货原因")
    private String returnReason;

    @Schema(description = "是否影响二次销售(0可/1不可)")
    private Integer isResalable;

    @Schema(description = "类型(1退货/2换货)")
    private Integer type;

    @Schema(description = "退货商品明细列表")
    private List<ReturnItemDTO> items;

    public Integer getSalesId() {
        return salesId;
    }

    public void setSalesId(Integer salesId) {
        this.salesId = salesId;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public Integer getIsResalable() { return isResalable; }
    public void setIsResalable(Integer isResalable) { this.isResalable = isResalable; }

    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }

    public List<ReturnItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReturnItemDTO> items) {
        this.items = items;
    }

    /**
     * 退货商品明细项
     */
    public static class ReturnItemDTO {
        @Schema(description = "商品ID")
        private Integer goodsId;

        @Schema(description = "退货/换货数量")
        private Integer returnCount;

        @Schema(description = "换货目标商品ID（type=2换货时使用）")
        private Integer exchangeGoodsId;

        public Integer getGoodsId() { return goodsId; }
        public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

        public Integer getReturnCount() { return returnCount; }
        public void setReturnCount(Integer returnCount) { this.returnCount = returnCount; }

        public Integer getExchangeGoodsId() { return exchangeGoodsId; }
        public void setExchangeGoodsId(Integer exchangeGoodsId) { this.exchangeGoodsId = exchangeGoodsId; }
    }
}
