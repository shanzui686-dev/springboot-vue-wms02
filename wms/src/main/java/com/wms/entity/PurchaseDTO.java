package com.wms.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 采购数据传输对象（用于接收前端提交的采购数据）
 * </p>
 *
 * @author wms
 * @since 2026-04-20
 */
@Schema(name = "PurchaseDTO", description = "采购数据传输对象")
public class PurchaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键（更新时使用）")
    private Integer id;

    @Schema(description = "采购单号")
    private String purchaseNo;

    @Schema(description = "供应商ID")
    private Integer supplierId;

    @Schema(description = "采购员ID")
    private Integer userId;

    @Schema(description = "采购日期")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime purchaseDate;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "状态(0:待审核, 1:已完成, 2:已取消)")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "采购明细列表")
    private List<PurchaseDetailItem> details;

    /**
     * 采购明细项（内部类）
     */
    @Schema(name = "PurchaseDetailItem", description = "采购明细项")
    public static class PurchaseDetailItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @Schema(description = "主键（更新时使用）")
        private Integer id;

        @Schema(description = "商品ID")
        private Integer goodsId;

        @Schema(description = "数量")
        private Integer count;

        @Schema(description = "单价")
        private BigDecimal price;

        @Schema(description = "小计金额")
        private BigDecimal subtotal;

        @Schema(description = "备注")
        private String remark;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
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

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        @Override
        public String toString() {
            return "PurchaseDetailItem{" +
                "id = " + id +
                ", goodsId = " + goodsId +
                ", count = " + count +
                ", price = " + price +
                ", subtotal = " + subtotal +
                ", remark = " + remark +
            "}";
        }
    }

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public List<PurchaseDetailItem> getDetails() {
        return details;
    }

    public void setDetails(List<PurchaseDetailItem> details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "PurchaseDTO{" +
            "id = " + id +
            ", purchaseNo = " + purchaseNo +
            ", supplierId = " + supplierId +
            ", userId = " + userId +
            ", purchaseDate = " + purchaseDate +
            ", totalAmount = " + totalAmount +
            ", status = " + status +
            ", remark = " + remark +
            ", details = " + details +
        "}";
    }
}
