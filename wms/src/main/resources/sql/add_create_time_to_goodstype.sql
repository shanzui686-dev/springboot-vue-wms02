-- 为 goodstype 表添加 create_time 字段（如果不存在）
-- 创建时间字段，用于记录分类的创建时间

ALTER TABLE goodstype 
ADD COLUMN IF NOT EXISTS create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER remark;

-- 更新已有数据的创建时间为当前时间
UPDATE goodstype SET create_time = NOW() WHERE create_time IS NULL;

-- 如果数据库不支持 IF NOT EXISTS，可以使用以下方式：
-- SELECT COUNT(*) INTO @col_exists 
-- FROM information_schema.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() 
--   AND TABLE_NAME = 'goodstype' 
--   AND COLUMN_NAME = 'create_time';
-- 
-- SET @sql = IF(@col_exists = 0, 
--     'ALTER TABLE goodstype ADD COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'' AFTER remark', 
--     'SELECT ''Column already exists'' AS message');
-- 
-- PREPARE stmt FROM @sql;
-- EXECUTE stmt;
-- DEALLOCATE PREPARE stmt;
