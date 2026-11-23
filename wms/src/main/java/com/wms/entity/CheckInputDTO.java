package com.wms.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * <p>
 * 盘点实盘数据录入DTO
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
@Schema(name = "CheckInputDTO", description = "盘点实盘数据录入DTO")
public class CheckInputDTO {

    @Schema(description = "盘点单ID")
    private Integer checkId;

    @Schema(description = "明细列表")
    private List<CheckDetailInputDTO> details;

    public Integer getCheckId() {
        return checkId;
    }

    public void setCheckId(Integer checkId) {
        this.checkId = checkId;
    }

    public List<CheckDetailInputDTO> getDetails() {
        return details;
    }

    public void setDetails(List<CheckDetailInputDTO> details) {
        this.details = details;
    }

    /**
     * 单条明细录入DTO
     */
    public static class CheckDetailInputDTO {
        @Schema(description = "明细ID")
        private Integer detailId;

        @Schema(description = "实盘数量")
        private Integer actualCount;

        public Integer getDetailId() {
            return detailId;
        }

        public void setDetailId(Integer detailId) {
            this.detailId = detailId;
        }

        public Integer getActualCount() {
            return actualCount;
        }

        public void setActualCount(Integer actualCount) {
            this.actualCount = actualCount;
        }
    }
}
