package com.wms.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 库存周转率统计结果
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@Data
public class TurnoverRateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 当前库存
     */
    private Integer currentStock;

    /**
     * 周期内总销量
     */
    private Integer totalSales;

    /**
     * 周转率（百分比）
     */
    private BigDecimal turnoverRate;
}
