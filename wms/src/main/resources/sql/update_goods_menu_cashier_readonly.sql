-- 商品信息、商品分类页面对收银员开放只读访问
-- menuRight 追加角色 2（收银员）
UPDATE menu SET menuRight = '0,1,2' WHERE menuCode = 'glist';
UPDATE menu SET menuRight = '0,1,2' WHERE menuCode = 'categry';
