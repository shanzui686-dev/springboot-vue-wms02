-- 为record表添加operation_type和ref_order_num字段

ALTER TABLE record 
ADD COLUMN operation_type VARCHAR(50) DEFAULT NULL COMMENT '操作类型（如：采购入库、销售出库、退货入库、盘点盈亏等）' AFTER status,
ADD COLUMN ref_order_num VARCHAR(100) DEFAULT NULL COMMENT '关联单据流水号' AFTER operation_type;

-- 为已有数据设置默认操作类型（可选）
-- UPDATE record SET operation_type = '其他' WHERE operation_type IS NULL;
