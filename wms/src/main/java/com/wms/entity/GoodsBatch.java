package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "GoodsBatch", description = "商品批次库存")
public class GoodsBatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "供应商ID")
    private Integer supplierId;

    @Schema(description = "仓库ID")
    private Integer storageId;

    @Schema(description = "进价/成本价")
    private BigDecimal purchasePrice;

    @Schema(description = "原始入库数量")
    private Integer initialCount;

    @Schema(description = "当前剩余数量")
    private Integer currentCount;

    @Schema(description = "来源采购单ID")
    private Integer purchaseId;

    @Schema(description = "入库时间")
    private LocalDateTime createTime;

    // ===== getter & setter =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    public Integer getStorageId() { return storageId; }
    public void setStorageId(Integer storageId) { this.storageId = storageId; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public Integer getInitialCount() { return initialCount; }
    public void setInitialCount(Integer initialCount) { this.initialCount = initialCount; }

    public Integer getCurrentCount() { return currentCount; }
    public void setCurrentCount(Integer currentCount) { this.currentCount = currentCount; }

    public Integer getPurchaseId() { return purchaseId; }
    public void setPurchaseId(Integer purchaseId) { this.purchaseId = purchaseId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    @Override
    public String toString() {
        return "GoodsBatch{" +
                "id=" + id +
                ", goodsId=" + goodsId +
                ", batchNo='" + batchNo + '\'' +
                ", supplierId=" + supplierId +
                ", storageId=" + storageId +
                ", purchasePrice=" + purchasePrice +
                ", initialCount=" + initialCount +
                ", currentCount=" + currentCount +
                ", purchaseId=" + purchaseId +
                ", createTime=" + createTime +
                '}';
    }
}


