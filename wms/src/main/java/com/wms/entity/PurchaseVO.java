package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 采购视图对象（包含供应商和采购员信息）
 * </p>
 *
 * @author wms
 * @since 2026-04-20
 */
@Schema(name = "PurchaseVO", description = "采购视图对象（包含供应商和采购员信息）")
public class PurchaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "采购单号")
    @TableField("purchase_no")
    private String purchaseNo;

    @Schema(description = "供应商ID")
    @TableField("supplier_id")
    private Integer supplierId;

    @Schema(description = "供应商名称")
    private String supplierName;

    @Schema(description = "采购员ID")
    @TableField("user_id")
    private Integer userId;

    @Schema(description = "采购员姓名")
    private String userName;

    @Schema(description = "采购日期")
    @TableField("purchase_date")
    private LocalDateTime purchaseDate;

    @Schema(description = "总金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @Schema(description = "状态(0:待审核, 1:已完成, 2:已取消)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPurchaseNo() {
        return purchaseNo;
    }

    public void setPurchaseNo(String purchaseNo) {
        this.purchaseNo = purchaseNo;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "PurchaseVO{" +
            "id = " + id +
            ", purchaseNo = " + purchaseNo +
            ", supplierId = " + supplierId +
            ", supplierName = " + supplierName +
            ", userId = " + userId +
            ", userName = " + userName +
            ", purchaseDate = " + purchaseDate +
            ", totalAmount = " + totalAmount +
            ", status = " + status +
            ", remark = " + remark +
        "}";
    }
}
