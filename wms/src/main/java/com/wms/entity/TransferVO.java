package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TransferVO", description = "调拨记录VO（含商品名、仓库名）")
public class TransferVO extends InventoryTransfer {

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "调出仓库名称")
    private String fromStorageName;

    @Schema(description = "调入仓库名称")
    private String toStorageName;

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public String getFromStorageName() { return fromStorageName; }
    public void setFromStorageName(String fromStorageName) { this.fromStorageName = fromStorageName; }

    public String getToStorageName() { return toStorageName; }
    public void setToStorageName(String toStorageName) { this.toStorageName = toStorageName; }
}
