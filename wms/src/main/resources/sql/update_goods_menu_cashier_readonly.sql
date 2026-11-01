-- 商品信息、商品分类页面对收银员开放只读访问
-- menuRight 追加角色 2（收银员）
-- 注意：一级菜单 goods 也必须放开，否则整个"商品管理"分组对收银员不可见
UPDATE menu SET menuRight = '0,1,2' WHERE menuCode = 'goods';
UPDATE menu SET menuRight = '0,1,2' WHERE menuCode = 'glist';
UPDATE menu SET menuRight = '0,1,2' WHERE menuCode = 'categry';
