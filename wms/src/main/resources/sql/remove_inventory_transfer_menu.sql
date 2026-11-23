-- ============================================
-- 移除库存调拨菜单项
-- 执行时间: 2026-10-19
-- ============================================

-- 删除"库存调拨"菜单项
DELETE FROM menu WHERE menuCode = 'trans' AND menuName = '库存调拨';

-- 验证删除结果
SELECT '库存调拨菜单项数量' AS description, COUNT(*) AS count FROM menu WHERE menuCode = 'trans';