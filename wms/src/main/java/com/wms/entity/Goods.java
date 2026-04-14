package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 
 * </p>
 *
 * @author wms
 * @since 2026-03-30
 */
@Schema(name = "Goods", description = "")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "货名")
    private String name;

    @Schema(description = "仓库")
    private Integer storage;

    @Schema(description = "分类")
    @TableField("goodsType")
    private Integer goodsType;

    @Schema(description = "数量")
    private Integer count;

    @Schema(description = "预警下限")
    @TableField("min_count")
    private Integer minCount;

    @Schema(description = "备注")
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStorage() {
        return storage;
    }

    public void setStorage(Integer storage) {
        this.storage = storage;
    }

    public Integer getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(Integer goodsType) {
        this.goodsType = goodsType;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Goods{" +
            "id = " + id +
            ", name = " + name +
            ", storage = " + storage +
            ", goodsType = " + goodsType +
            ", count = " + count +
            ", minCount = " + minCount +
            ", remark = " + remark +
        "}";
    }
}
