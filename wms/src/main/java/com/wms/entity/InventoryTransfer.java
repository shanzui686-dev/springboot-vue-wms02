package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "InventoryTransfer", description = "库存调拨表")
public class InventoryTransfer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "调拨单号")
    private String transferNo;

    @Schema(description = "调出仓库ID")
    private Integer fromStorageId;

    @Schema(description = "调入仓库ID")
    private Integer toStorageId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "调拨数量")
    private Integer count;

    @Schema(description = "状态(0:待审核, 1:已通过, 2:已拒绝)")
    private Integer status;

    @Schema(description = "创建人ID")
    private Integer creatorId;

    @Schema(description = "审核人ID")
    private Integer auditorId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "备注")
    private String remark;

    // ===== getter & setter =====
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String transferNo) { this.transferNo = transferNo; }

    public Integer getFromStorageId() { return fromStorageId; }
    public void setFromStorageId(Integer fromStorageId) { this.fromStorageId = fromStorageId; }

    public Integer getToStorageId() { return toStorageId; }
    public void setToStorageId(Integer toStorageId) { this.toStorageId = toStorageId; }

    public Integer getGoodsId() { return goodsId; }
    public void setGoodsId(Integer goodsId) { this.goodsId = goodsId; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

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

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return "InventoryTransfer{" +
            "id=" + id +
            ", transferNo='" + transferNo + '\'' +
            ", fromStorageId=" + fromStorageId +
            ", toStorageId=" + toStorageId +
            ", goodsId=" + goodsId +
            ", count=" + count +
            ", status=" + status +
            ", creatorId=" + creatorId +
            ", auditorId=" + auditorId +
            ", createTime=" + createTime +
            ", auditTime=" + auditTime +
            ", remark='" + remark + '\'' +
        '}';
    }
}
