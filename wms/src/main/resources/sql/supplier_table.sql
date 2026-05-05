-- 供应商表建表SQL
CREATE TABLE IF NOT EXISTS `supplier` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(100) NOT NULL COMMENT '供应商名称',
  `contact` VARCHAR(50) DEFAULT NULL COMMENT '联系人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 插入测试数据
INSERT INTO `supplier` (`name`, `contact`, `phone`, `address`, `status`, `remark`) VALUES
('北京食品供应公司', '张三', '13800138001', '北京市朝阳区xxx路xxx号', 1, '主要供应食品饮料'),
('上海日化用品厂', '李四', '13800138002', '上海市浦东新区xxx街xxx号', 1, '日化用品供应商'),
('广州生鲜配送中心', '王五', '13800138003', '广州市天河区xxx大道xxx号', 1, '新鲜蔬菜水果供应'),
('深圳电子产品供应商', '赵六', '13800138004', '深圳市南山区xxx科技园', 0, '已暂停合作');
