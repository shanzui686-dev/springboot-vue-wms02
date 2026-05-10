package com.wms.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 智能补货建议结果
 * </p>
 *
 * @author wms
 * @since 2026-04-18
 */
@Data
public class RestockSuggestionVO implements Serializable {

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
     * 商品条码
     */
    private String barcode;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 日均销量
     */
    private BigDecimal dailyAverageSales;

    /**
     * 当前库存
     */
    private Integer currentStock;

    /**
     * 建议补货量
     */
    private Integer suggestQuantity;

    /**
     * 安全库存
     */
    private Integer safetyStock;
}
