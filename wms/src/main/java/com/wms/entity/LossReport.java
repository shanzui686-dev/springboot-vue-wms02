package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "LossReport", description = "损耗单")
public class LossReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "损耗单号")
    private String lossNo;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "批次ID")
    private Integer batchId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "损耗数量")
    private Integer lossCount;

    @Schema(description = "损耗类型(1:库存损耗 2:销售损耗)")
    private Integer lossType;

    @Schema(description = "进价快照")
    private BigDecimal purchasePrice;

    @Schema(description = "损耗金额")
    private BigDecimal lossAmount;

    @Schema(description = "损耗原因")
    private String reason;

    @Schema(description = "状态(0:待审核 1:已处理 2:已拒绝)")
    private Integer status;

    @Schema(description = "上报人ID")
    private Integer reporterId;

    @Schema(description = "审核人ID")
    private Integer auditorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    // ===== getter & setter =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLossNo() { return lossNo; }
    public void setLossNo(String lossNo) { this.lossNo = lossNo; }

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public Integer getBatchId() { return batchId; }
    public void setBatchId(Integer batchId) { this.batchId = batchId; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public Integer getLossCount() { return lossCount; }
    public void setLossCount(Integer lossCount) { this.lossCount = lossCount; }

    public Integer getLossType() { return lossType; }
    public void setLossType(Integer lossType) { this.lossType = lossType; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getLossAmount() { return lossAmount; }
    public void setLossAmount(BigDecimal lossAmount) { this.lossAmount = lossAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getReporterId() { return reporterId; }
    public void setReporterId(Integer reporterId) { this.reporterId = reporterId; }

    public Integer getAuditorId() { return auditorId; }
    public void setAuditorId(Integer auditorId) { this.auditorId = auditorId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }

    @Override
    public String toString() {
        return "LossReport{" +
                "id=" + id +
                ", lossNo='" + lossNo + '\'' +
                ", goodsId=" + goodsId +
                ", batchId=" + batchId +
                ", batchNo='" + batchNo + '\'' +
                ", lossCount=" + lossCount +
                ", lossType=" + lossType +
                ", purchasePrice=" + purchasePrice +
                ", lossAmount=" + lossAmount +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", reporterId=" + reporterId +
                ", auditorId=" + auditorId +
                ", createTime=" + createTime +
                ", auditTime=" + auditTime +
                '}';
    }
}
