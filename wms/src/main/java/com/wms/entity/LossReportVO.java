package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "LossReportVO", description = "损耗单VO（连表查商品名/上报人）")
public class LossReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String lossNo;
    private Integer goodsId;
    private String goodsName;
    private Integer batchId;
    private String batchNo;
    private Integer lossCount;
    private Integer lossType;
    private BigDecimal purchasePrice;
    private BigDecimal lossAmount;
    private String reason;
    private Integer status;
    private Integer reporterId;
    private String reporterName;
    private Integer auditorId;
    private String auditorName;
    private LocalDateTime createTime;
    private LocalDateTime auditTime;

    // ===== getter & setter =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getLossNo() { return lossNo; }
    public void setLossNo(String lossNo) { this.lossNo = lossNo; }

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

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

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public Integer getAuditorId() { return auditorId; }
    public void setAuditorId(Integer auditorId) { this.auditorId = auditorId; }

    public String getAuditorName() { return auditorName; }
    public void setAuditorName(String auditorName) { this.auditorName = auditorName; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }
}
