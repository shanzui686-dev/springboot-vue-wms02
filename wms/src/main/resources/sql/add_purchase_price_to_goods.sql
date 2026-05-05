-- 为 goods 表添加 purchase_price 字段（如果不存在）
-- 进价字段，用于记录商品的进货价格

ALTER TABLE goods 
ADD COLUMN IF NOT EXISTS purchase_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '进货价' AFTER retail_price;

-- 如果数据库不支持 IF NOT EXISTS，可以使用以下方式：
-- 先检查字段是否存在，不存在则添加
-- SELECT COUNT(*) INTO @col_exists 
-- FROM information_schema.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() 
--   AND TABLE_NAME = 'goods' 
--   AND COLUMN_NAME = 'purchase_price';
-- 
-- SET @sql = IF(@col_exists = 0, 
--     'ALTER TABLE goods ADD COLUMN purchase_price DECIMAL(10,2) DEFAULT 0.00 COMMENT ''进货价'' AFTER retail_price', 
--     'SELECT ''Column already exists'' AS message');
-- 
-- PREPARE stmt FROM @sql;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;
