-- ============================================
-- 更新补货建议报表权限：添加库存管理员权限
-- 执行时间: 2026-10-19
-- ============================================

-- 更新补货建议报表菜单权限为店长+库存管理员
UPDATE menu SET menuRight = '0,1' WHERE menuCode = 'restock' AND menuName = '补货建议报表';

-- 验证更新结果
SELECT menuCode, menuName, menuRight FROM menu WHERE menuCode = 'restock';