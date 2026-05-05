package com.wms.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 毛利统计结果
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@Data
public class GrossProfitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日期
     */
    private String date;

    /**
     * 毛利
     */
    private BigDecimal grossProfit;
}
