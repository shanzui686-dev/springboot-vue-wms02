package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 库存盘点明细表
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
@Schema(name = "InventoryCheckDetail", description = "库存盘点明细表")
public class InventoryCheckDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "盘点单ID")
    private Integer checkId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "账面库存(预期数量)")
    private Integer expectedCount;

    @Schema(description = "实盘库存(实际数量)")
    private Integer actualCount;

    @Schema(description = "差异数量(实际-预期)")
    private Integer diffCount;

    @Schema(description = "备注")
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCheckId() {
        return checkId;
    }

    public void setCheckId(Integer checkId) {
        this.checkId = checkId;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public Integer getExpectedCount() {
        return expectedCount;
    }

    public void setExpectedCount(Integer expectedCount) {
        this.expectedCount = expectedCount;
    }

    public Integer getActualCount() {
        return actualCount;
    }

    public void setActualCount(Integer actualCount) {
        this.actualCount = actualCount;
        this.diffCount = actualCount - expectedCount;
    }

    public Integer getDiffCount() {
        return diffCount;
    }

    public void setDiffCount(Integer diffCount) {
        this.diffCount = diffCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "InventoryCheckDetail{" +
            "id = " + id +
            ", checkId = " + checkId +
            ", goodsId = " + goodsId +
            ", expectedCount = " + expectedCount +
            ", actualCount = " + actualCount +
            ", diffCount = " + diffCount +
            ", remark = " + remark +
        "}";
    }
}
