package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 商品视图对象（包含供应商信息）
 * </p>
 *
 * @author wms
 * @since 2026-04-17
 */
@Schema(name = "GoodsVO", description = "商品视图对象（包含供应商信息）")
public class GoodsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "商品名")
    private String name;

    @Schema(description = "仓库")
    private Integer storage;

    @Schema(description = "分类")
    @TableField("goodsType")
    private Integer goodsType;

    @Schema(description = "数量")
    private Integer count;

    @Schema(description = "预占数量")
    @TableField("reserved_count")
    private Integer reservedCount;

    @Schema(description = "预警下限")
    @TableField("min_count")
    private Integer minCount;

    @Schema(description = "条形码")
    private String barcode;

    @Schema(description = "规格")
    private String specs;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "零售价")
    @TableField("retail_price")
    private BigDecimal retailPrice;

    @Schema(description = "进货价")
    @TableField("purchase_price")
    private BigDecimal purchasePrice;

    @Schema(description = "供应商ID")
    @TableField("supplier_id")
    private Integer supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "仓库名称")
    private String warehouseName;

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

    public Integer getReservedCount() {
        return reservedCount;
    }

    public void setReservedCount(Integer reservedCount) {
        this.reservedCount = reservedCount;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(BigDecimal retailPrice) {
        this.retailPrice = retailPrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "GoodsVO{" +
            "id = " + id +
            ", name = " + name +
            ", storage = " + storage +
            ", goodsType = " + goodsType +
            ", count = " + count +
            ", reservedCount = " + reservedCount +
            ", minCount = " + minCount +
            ", barcode = " + barcode +
            ", specs = " + specs +
            ", unit = " + unit +
            ", retailPrice = " + retailPrice +
            ", purchasePrice = " + purchasePrice +
            ", supplierId = " + supplierId +
            ", supplierName = " + supplierName +
            ", categoryName = " + categoryName +
            ", warehouseName = " + warehouseName +
            ", remark = " + remark +
        "}";
    }
}
