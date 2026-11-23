-- 修复 sales_return 表的 order_num 字段，添加默认值
ALTER TABLE sales_return MODIFY COLUMN order_num VARCHAR(100) DEFAULT NULL COMMENT '退货单号';
