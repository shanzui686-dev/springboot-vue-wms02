-- ============================================
-- 库存盘点与调拨模块 - 菜单添加脚本
-- 创建时间: 2026-05-10
-- 支持重复执行（先删除再插入）
-- ============================================

-- 先删除可能存在的旧数据
DELETE FROM menu WHERE id IN (999, 9990, 9991);

-- 1. 添加一级菜单：库存管理（父菜单），点击跳转到库存盘点
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight, menuComponent, menuIcon) VALUES
(999, 'inv', '库存管理', '1', '0', '/Check', '0,1', 'Index', 'Box');

-- 2. 添加二级菜单：库存盘点
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight, menuComponent, menuIcon) VALUES
(9990, 'check', '库存盘点', '2', 'inv', '/Check', '0,1', 'inventory/Check', 'DocumentChecked');

-- 3. 添加二级菜单：库存调拨
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight, menuComponent, menuIcon) VALUES
(9991, 'transfer', '库存调拨', '2', 'inv', '/Transfer', '0,1', 'inventory/Transfer', 'Sort');

-- ============================================
-- 说明
-- ============================================
-- menuLevel: 1=一级菜单，2=二级菜单
-- menuParentCode: 父菜单的menuCode（0表示无父菜单）
-- menuClick: 路由路径（一级菜单为空，二级菜单为路由路径）
-- menuRight: 权限控制（0=超级管理员，1=管理员，2=收银员，逗号分隔）
-- menuComponent: Vue组件路径（相对于 src/components/）
-- menuIcon: Element Plus 图标名称
