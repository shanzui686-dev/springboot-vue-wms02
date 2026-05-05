-- 检查并添加 order_num 字段到 sales 表
-- 如果字段已存在则跳过

-- 方式 1：直接添加（如果字段不存在）
ALTER TABLE sales ADD COLUMN order_num VARCHAR(50) DEFAULT NULL COMMENT '订单流水号';

-- 方式 2：如果上面的语句报错说字段已存在，执行以下语句修改字段：
-- ALTER TABLE sales MODIFY COLUMN order_num VARCHAR(50) DEFAULT NULL COMMENT '订单流水号';

-- 为 order_num 字段添加索引，提高查询效率
CREATE INDEX idx_order_num ON sales(order_num);

-- 为 create_time 字段添加索引，提高时间范围查询效率
CREATE INDEX idx_create_time ON sales(create_time);

-- 更新现有数据（如果没有 order_num，可以使用 id 作为流水号）
UPDATE sales SET order_num = CONCAT('SO', LPAD(id, 8, '0')) WHERE order_num IS NULL OR order_num = '';
