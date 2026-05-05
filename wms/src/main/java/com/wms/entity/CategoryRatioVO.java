package com.wms.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 分类销售占比统计结果
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@Data
public class CategoryRatioVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 销量
     */
    private Integer totalQuantity;

    /**
     * 销售额
     */
    private BigDecimal totalAmount;

    /**
     * 占比（百分比）
     */
    private BigDecimal ratio;
}
