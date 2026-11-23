package com.wms.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 智能补货建议导出VO
 * </p>
 *
 * @author wms
 * @since 2026-05-07
 */
@Data
public class RestockExportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "商品条码", index = 0)
    private String barcode;

    @ExcelProperty(value = "商品名称", index = 1)
    private String goodsName;

    @ExcelProperty(value = "分类", index = 2)
    private String categoryName;

    @ExcelProperty(value = "当前库存", index = 3)
    private Integer currentStock;

    @ExcelProperty(value = "安全库存", index = 4)
    private Integer safetyStock;

    @ExcelProperty(value = "近30天日均销量", index = 5)
    private String dailyAverageSales;

    @ExcelProperty(value = "建议采购量", index = 6)
    private Integer suggestQuantity;
}
