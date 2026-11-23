package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(name = "PurchaseReturnDTO", description = "采购退货请求")
public class PurchaseReturnDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原采购单ID")
    private Integer purchaseId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "goods_batch ID（关联到具体批次库存）")
    private Integer batchId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "退货数量")
    private Integer returnCount;

    @Schema(description = "退货原因")
    private String reason;

    @Schema(description = "创建人ID（采购员）")
    private Integer creatorId;

    public Integer getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Integer purchaseId) { this.purchaseId = purchaseId; }
    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }
    public Integer getBatchId() { return batchId; }
    public void setBatchId(Integer batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Integer getReturnCount() { return returnCount; }
    public void setReturnCount(Integer returnCount) { this.returnCount = returnCount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    @Override
    public String toString() {
        return "PurchaseReturnDTO{purchaseId=" + purchaseId + ", goodsId=" + goodsId +
            ", batchId=" + batchId + ", batchNo='" + batchNo + '\'' +
            ", returnCount=" + returnCount + ", reason='" + reason + '\'' +
            ", creatorId=" + creatorId + '}';
    }
}