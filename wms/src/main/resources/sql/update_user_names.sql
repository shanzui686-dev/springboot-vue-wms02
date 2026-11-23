-- ============================================
-- 更新用户名称以匹配新的角色体系
-- 执行时间: 2026-05-07
-- ============================================

-- 更新用户名称
UPDATE user SET name = '系统管理员' WHERE id = 1 AND roleId = 0;
UPDATE user SET name = '库存管理员' WHERE id = 2 AND roleId = 1;
UPDATE user SET name = '收银员王五' WHERE id = 5 AND roleId = 2 AND name = '普通用户王五';
UPDATE user SET name = '收银员赵六' WHERE id = 6 AND roleId = 2 AND name = '普通用户赵六';

-- 验证更新结果
SELECT id, no, name, roleId,
       CASE roleId 
           WHEN 0 THEN '系统管理员'
           WHEN 1 THEN '库存管理员'
           WHEN 2 THEN '收银员'
           ELSE '未知角色'
       END as roleName
FROM user ORDER BY roleId, id;
