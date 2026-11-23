package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 库存调拨DTO
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
@Schema(name = "TransferDTO", description = "库存调拨DTO")
public class TransferDTO {

    @Schema(description = "调出仓库ID")
    private Integer fromStorageId;

    @Schema(description = "调入仓库ID")
    private Integer toStorageId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "调拨数量")
    private Integer count;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作人ID")
    private Integer creatorId;

    public Integer getFromStorageId() {
        return fromStorageId;
    }

    public void setFromStorageId(Integer fromStorageId) {
        this.fromStorageId = fromStorageId;
    }

    public Integer getToStorageId() {
        return toStorageId;
    }

    public void setToStorageId(Integer toStorageId) {
        this.toStorageId = toStorageId;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }
}
