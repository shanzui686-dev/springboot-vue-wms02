-- ============================================
-- 退换货模块数据库建表脚本
-- ============================================

-- 1. 创建退货主表 sales_return
CREATE TABLE IF NOT EXISTS `sales_return` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `return_no` VARCHAR(50) NOT NULL COMMENT '退货单号',
  `sales_id` INT NOT NULL COMMENT '原销售单ID',
  `return_reason` VARCHAR(200) DEFAULT NULL COMMENT '退货原因',
  `return_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '退货总金额',
  `status` INT NOT NULL DEFAULT 0 COMMENT '状态：0待退款，1已退款',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_return_no` (`return_no`),
  KEY `idx_sales_id` (`sales_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货主表';

-- 2. 创建退货明细表 sales_return_detail
CREATE TABLE IF NOT EXISTS `sales_return_detail` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `return_id` INT NOT NULL COMMENT '退货单ID',
  `goods_id` INT NOT NULL COMMENT '商品ID',
  `return_count` INT NOT NULL COMMENT '退货数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`),
  KEY `idx_return_id` (`return_id`),
  KEY `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退货明细表';

-- ============================================
-- 说明：
-- 1. sales_return 表存储退货单主信息
-- 2. sales_return_detail 表存储退货商品明细
-- 3. 退货流程：
--    - 申请退货：插入 sales_return (status=0) + sales_return_detail
--    - 确认退款：更新 sales_return (status=1) + 回滚 goods 表库存
-- ============================================
