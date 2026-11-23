-- ============================================
-- WMS 角色命名与菜单结构重构脚本
-- 执行时间: 2026-10-17
-- 目标: 3角色(店长0/库存管理员1/收银员2) + 6大模块
-- 注意: menuCode 列为 VARCHAR(7)，所有编码≤7字符
-- ============================================

-- 1. 清空现有菜单数据
DELETE FROM menu;

-- 2. 插入新的一级菜单（5大模块）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight, menuComponent, menuIcon) VALUES
-- 一级菜单
(1,  'usermgt',   '用户管理', '1', '0', '', '0',     'Index', 'User'),
(2,  'goods',     '商品管理', '1', '0', '', '0,1',   'Index', 'Shopping'),
(3,  'purchase',  '采购管理', '1', '0', '', '0,1',   'Index', 'ShoppingCart'),
(4,  'invent',    '库存管理', '1', '0', '', '0,1',   'Index', 'Box'),
(5,  'sales',     '销售管理', '1', '0', '', '0,2',   'Index', 'Money'),

-- 二级菜单：用户管理
(10, 'user',      '用户账号管理',   '2', 'usermgt',  '/User',        '0',     'user/UserManage',       'User'),
(11, 'admin',     '管理员账号管理', '2', 'usermgt',  '/Admin',       '0',     'admin/AdminManage',     'UserFilled'),

-- 二级菜单：商品管理（店长+库存管理员）
(20, 'glist',     '商品信息',       '2', 'goods',    '/Goods',       '0,1',   'goods/GoodsManage',     'Goods'),
(21, 'categry',   '商品分类',       '2', 'goods',    '/Goodstype',   '0,1',   'goodstype/GoodstypeManage', 'Grid'),

-- 二级菜单：采购管理
(30, 'supply',    '供应商信息维护', '2', 'purchase', '/Supplier',    '0,1',   'supplier/SupplierManage', 'Van'),
(31, 'purlist',   '采购入库',       '2', 'purchase', '/PurchaseManage','0,1',  'purchase/PurchaseManage', 'ShoppingCart'),
(32, 'purrview',  '采购退回审核',   '2', 'purchase', '/PurchaseReturnReview','0','purchase/PurchaseReturnReview','RefreshLeft'),

-- 二级菜单：库存管理
(40, 'check',     '库存盘点',       '2', 'invent',   '/Check',       '0,1',   'inventory/Check',       'DocumentChecked'),
(41, 'restock',   '补货建议报表',   '2', 'invent',   '/RestockSuggest','0,1',  'stats/RestockSuggest',  'Present'),

-- 二级菜单：销售管理
(50, 'cashds',    '收银结算',       '2', 'sales',    '/CashierDesk', '0,2',   'cashinerdesk/CashierDesk','Money'),
(51, 'salesr',    '销售记录查询',   '2', 'sales',    '/SalesRecord', '0,2',   'record/SalesRecord',    'Sell'),
(52, 'return',    '退换货处理',     '2', 'sales',    '/ReturnManage','0,2',   'record/ReturnManage',   'RefreshLeft');