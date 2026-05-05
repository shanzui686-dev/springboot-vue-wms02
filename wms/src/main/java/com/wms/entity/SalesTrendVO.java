package com.wms.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 销售趋势统计结果
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@Data
public class SalesTrendVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private String date;

    /**
     * 销售额
     */
    private BigDecimal totalAmount;

    /**
     * 订单量
     */
    private Integer orderCount;
}
