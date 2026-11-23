package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 采购入库请求 DTO（店长审核通过后填写仓库和入库数量）
 */
@Schema(name = "InboundDTO", description = "采购入库请求")
public class InboundDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "采购单ID")
    private Integer purchaseId;

    @Schema(description = "入库仓库ID")
    private Integer storageId;

    @Schema(description = "入库明细列表")
    private List<InboundItem> items;

    @Schema(name = "InboundItem", description = "入库明细项")
    public static class InboundItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "采购明细ID")
        private Integer detailId;

        @Schema(description = "实际入库数量")
        private Integer actualCount;

        public Integer getDetailId() { return detailId; }
        public void setDetailId(Integer detailId) { this.detailId = detailId; }
        public Integer getActualCount() { return actualCount; }
        public void setActualCount(Integer actualCount) { this.actualCount = actualCount; }

        @Override
        public String toString() {
            return "InboundItem{detailId=" + detailId + ", actualCount=" + actualCount + "}";
        }
    }

    public Integer getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Integer purchaseId) { this.purchaseId = purchaseId; }
    public Integer getStorageId() { return storageId; }
    public void setStorageId(Integer storageId) { this.storageId = storageId; }
    public List<InboundItem> getItems() { return items; }
    public void setItems(List<InboundItem> items) { this.items = items; }

    @Override
    public String toString() {
        return "InboundDTO{purchaseId=" + purchaseId + ", storageId=" + storageId + ", items=" + items + "}";
    }
}