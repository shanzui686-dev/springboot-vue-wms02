package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>
 * 
 * </p>
 *
 * @author wms
 * @since 2026-04-01
 */
@Schema(name = "Record", description = "")
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "货品id")
    private Integer goods;

    @Schema(description = "取货人/补货人")
    @TableField("userid")
    private Integer userId;

    @Schema(description = "操作人id")
    @TableField("admin_id")
    private Integer adminId;

    @Schema(description = "数量")
    private Integer count;

    @Schema(description = "操作时间")
    private LocalDateTime createtime;

    @Schema(description = "备注")
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGoods() {
        return goods;
    }

    public void setGoods(Integer goods) {
        this.goods = goods;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public LocalDateTime getCreatetime() {
        return createtime;
    }

    public void setCreatetime(LocalDateTime createtime) {
        this.createtime = createtime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "Record{" +
            "id = " + id +
            ", goods = " + goods +
            ", userId = " + userId +
            ", adminId = " + adminId +
            ", count = " + count +
            ", createtime = " + createtime +
            ", remark = " + remark +
        "}";
    }
}
