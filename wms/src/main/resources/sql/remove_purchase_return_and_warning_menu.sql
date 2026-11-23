-- ============================================
-- 移除采购退回和库存预警菜单项
-- 执行时间: 2026-10-19
-- ============================================

-- 删除"采购退回"菜单项
DELETE FROM menu WHERE menuCode = 'purchrt' AND menuName = '采购退回';

-- 删除"库存预警"菜单项
DELETE FROM menu WHERE menuCode = 'warning' AND menuName = '库存预警';

-- 验证删除结果
SELECT '采购退回菜单项数量' AS description, COUNT(*) AS count FROM menu WHERE menuCode = 'purchrt'
UNION ALL
SELECT '库存预警菜单项数量' AS description, COUNT(*) AS count FROM menu WHERE menuCode = 'warning';