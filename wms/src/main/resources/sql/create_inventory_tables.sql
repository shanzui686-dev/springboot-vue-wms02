-- ============================================
-- 库存盘点与调拨模块 - 建表脚本
-- 创建时间: 2026-05-10
-- 支持重复执行（先删除旧表再创建）
-- ============================================

-- 按依赖关系逆序删除表
DROP TABLE IF EXISTS `inventory_check_detail`;
DROP TABLE IF EXISTS `inventory_transfer`;
DROP TABLE IF EXISTS `inventory_check`;

-- 1. 盘点主表
CREATE TABLE `inventory_check` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `check_no` VARCHAR(50) NOT NULL COMMENT '盘点单号(如 CHK20231024001)',
  `storage_id` INT NOT NULL COMMENT '盘点仓库ID',
  `status` INT DEFAULT 0 COMMENT '状态(0:待录入, 1:待审核, 2:已完成)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `creator_id` INT NOT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_check_no` (`check_no`),
  KEY `idx_storage_id` (`storage_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点主表';

-- 2. 盘点明细表（含外键约束）
CREATE TABLE `inventory_check_detail` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `check_id` INT NOT NULL COMMENT '关联主表ID',
  `goods_id` INT NOT NULL COMMENT '商品ID',
  `expected_count` INT NOT NULL COMMENT '账面库存(快照)',
  `actual_count` INT DEFAULT 0 COMMENT '实盘库存',
  `diff_count` INT DEFAULT 0 COMMENT '盈亏数量(实盘-账面)',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_check_id` (`check_id`),
  KEY `idx_goods_id` (`goods_id`),
  CONSTRAINT `fk_check_detail_check` FOREIGN KEY (`check_id`) REFERENCES `inventory_check` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存盘点明细表';

-- 3. 库存调拨表
CREATE TABLE `inventory_transfer` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `transfer_no` VARCHAR(50) NOT NULL COMMENT '调拨单号',
  `goods_id` INT NOT NULL COMMENT '调拨商品ID',
  `from_storage_id` INT NOT NULL COMMENT '调出仓库ID',
  `to_storage_id` INT NOT NULL COMMENT '调入仓库ID',
  `count` INT NOT NULL COMMENT '调拨数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调拨时间',
  `creator_id` INT NOT NULL COMMENT '操作人ID',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`),
  KEY `idx_goods_id` (`goods_id`),
  KEY `idx_from_storage` (`from_storage_id`),
  KEY `idx_to_storage` (`to_storage_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存调拨表';

-- ============================================
-- 说明
-- ============================================
-- 1. 盘点流程：
--    - 创建盘点单 (status=0 待录入) -> 生成库存快照
--    - 录入实盘数据 (status=1 待审核) -> 自动计算盈亏
--    - 审核盘点单 (status=2 已完成) -> 调整商品库存 + 插入流水记录
--
-- 2. 调拨流程：
--    - 执行调拨 -> 扣减调出仓库存 + 增加调入仓库存 + 双向流水记账
--
-- 3. 字段说明：
--    - diff_count = actual_count - expected_count
--    - 正数表示盘盈，负数表示盘亏
--    - 调拨时 record 表双向记账（一条调出负数、一条调入正数）
