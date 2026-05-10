package com.wms.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 记录导出DTO
 */
@Data
public class RecordExportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelProperty(value = "记录ID", index = 0)
    private Integer id;

    @ExcelProperty(value = "商品名称", index = 1)
    private String goodsName;

    @ExcelProperty(value = "仓库名称", index = 2)
    private String storageName;

    @ExcelProperty(value = "分类名称", index = 3)
    private String goodsTypeName;

    @ExcelProperty(value = "操作类型", index = 4)
    private String operationType;

    @ExcelProperty(value = "关联单据号", index = 5)
    private String refOrderNum;

    @ExcelProperty(value = "数量", index = 6)
    private Integer count;

    @ExcelProperty(value = "申请人", index = 7)
    private String username;

    @ExcelProperty(value = "操作人", index = 8)
    private String adminname;

    @ExcelProperty(value = "操作时间", index = 9)
    private String createtime;

    @ExcelProperty(value = "备注", index = 10)
    private String remark;

    @ExcelProperty(value = "状态", index = 11)
    private String statusDesc;
}
