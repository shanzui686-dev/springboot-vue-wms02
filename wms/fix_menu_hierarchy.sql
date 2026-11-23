-- ============================================================
-- 菜单层级重组 + 新增缺失菜单
-- 0=店长  1=库管员  2=收银员
-- ============================================================

-- 1. 新建一级菜单：用户管理（店长可见）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight)
VALUES (16, 'userMgmt', '用户管理', '1', '0', '', '0');

-- 2. 管理员管理 → 用户管理子菜单
UPDATE menu SET menuLevel = '2', menuParentCode = 'userMgmt', menuClick = '/Admin' WHERE id = 1;

-- 3. 收银员管理 → 用户管理子菜单
UPDATE menu SET menuLevel = '2', menuParentCode = 'userMgmt', menuClick = '/User' WHERE id = 2;

-- 4. 新建一级菜单：商品管理（所有角色可见）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight)
VALUES (17, 'goodsMgmt', '商品管理', '1', '0', '', '0,1,2');

-- 5. 商品分类管理 → 商品管理子菜单
UPDATE menu SET menuLevel = '2', menuParentCode = 'goodsMgmt', menuClick = '/Goodstype' WHERE id = 4;

-- 6. 商品信息管理 → 商品管理子菜单
UPDATE menu SET menuLevel = '2', menuParentCode = 'goodsMgmt', menuClick = '/Goods' WHERE id = 5;

-- 7. 库存管理父菜单：去掉错误的 menuClick
UPDATE menu SET menuClick = '' WHERE id = 14;

-- 8. 新增数据大屏（仅店长）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight)
VALUES (18, 'dashboard', '数据大屏', '1', '0', '/Dashboard', '0');

-- 9. 新增退换货管理（店长+收银员）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight)
VALUES (19, 'returnMgmt', '退换货管理', '1', '0', '/ReturnManage', '0,2');

-- 10. 新增损耗管理（所有角色）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight)
VALUES (20, 'lossMgmt', '损耗管理', '1', '0', '/LossReport', '0,1,2');
