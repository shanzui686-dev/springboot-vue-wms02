-- ============================================
-- 更新店长权限：店长可以访问所有页面
-- 执行时间: 2026-10-19
-- ============================================

-- 更新商品管理权限（店长+库存管理员）
UPDATE menu SET menuRight = '0,1' WHERE menuCode IN ('glist', 'categry');

-- 更新供应商管理权限（店长+库存管理员）
UPDATE menu SET menuRight = '0,1' WHERE menuCode = 'supply';

-- 更新库存管理权限（店长+库存管理员）
UPDATE menu SET menuRight = '0,1' WHERE menuCode IN ('check', 'trans');

-- 更新收银结算权限（店长+收银员）
UPDATE menu SET menuRight = '0,2' WHERE menuCode = 'cashds';

-- 验证更新结果
SELECT menuCode, menuName, menuRight FROM menu WHERE menuLevel = '2' ORDER BY id;