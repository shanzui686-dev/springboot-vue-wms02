package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(name = "PurchaseReturn", description = "采购退货单")
public class PurchaseReturn implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "退货单号")
    private String returnNo;

    @Schema(description = "原采购单ID")
    private Integer purchaseId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "goods_batch ID")
    private Integer batchId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "退货数量")
    private Integer returnCount;

    @Schema(description = "退货原因")
    private String reason;

    @Schema(description = "状态(0:新建, 1:待审核, 2:已完成, 3:已拒绝)")
    private Integer status;

    @Schema(description = "创建人ID（采购员）")
    private Integer creatorId;

    @Schema(description = "审核人ID（店长）")
    private Integer auditorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    // ===== getter & setter =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReturnNo() { return returnNo; }
    public void setReturnNo(String returnNo) { this.returnNo = returnNo; }
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
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }
    public Integer getAuditorId() { return auditorId; }
    public void setAuditorId(Integer auditorId) { this.auditorId = auditorId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }

    @Override
    public String toString() {
        return "PurchaseReturn{" +
            "id=" + id + ", returnNo='" + returnNo + '\'' +
            ", purchaseId=" + purchaseId + ", goodsId=" + goodsId +
            ", batchId=" + batchId + ", batchNo='" + batchNo + '\'' +
            ", returnCount=" + returnCount + ", reason='" + reason + '\'' +
            ", status=" + status + ", creatorId=" + creatorId +
            ", auditorId=" + auditorId +
            ", createTime=" + createTime + ", auditTime=" + auditTime + '}';
    }
}