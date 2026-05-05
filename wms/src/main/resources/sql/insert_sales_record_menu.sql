-- 在 menu 表中插入"销售记录查询"菜单项
-- 数据库字段使用驼峰命名
-- 只插入必填字段，避免字段长度问题

INSERT INTO menu (menuName, menuIcon, menuClick, menuComponent, menuRight, menuLevel, menuParentCode, menuCode) 
VALUES ('销售记录查询', 'el-icon-s-order', 'SalesRecord', 'record/SalesRecord.vue', '0', '1', '0', 'sales');

-- 如果上面还是报错，使用最简版本（只插入核心字段）：
-- INSERT INTO menu (menuName, menuClick, menuComponent, menuRight) 
-- VALUES ('销售记录查询', 'SalesRecord', 'record/SalesRecord.vue', '0');
