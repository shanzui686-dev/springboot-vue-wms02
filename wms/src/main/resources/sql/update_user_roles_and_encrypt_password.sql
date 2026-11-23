-- ============================================
-- 用户角色调整及密码MD5加密
-- 说明：
--   role_id = 0: 系统管理员
--   role_id = 1: 库存管理员  
--   role_id = 2: 收银员
-- ============================================

-- 1. 扩展password字段长度以支持MD5加密（32位）
ALTER TABLE user MODIFY COLUMN password VARCHAR(100) NOT NULL COMMENT '密码';

-- 2. 更新角色名称映射（保持role_id数值不变，仅调整业务含义）
-- role_id=0 保持为系统管理员
-- role_id=1 从"管理员"改为"库存管理员"
-- role_id=2 从"普通用户"改为"收银员"

-- 3. 对所有用户密码进行MD5加密
-- 注意：如果密码已经是MD5加密的，请勿重复执行此语句
-- 示例：明文密码 '123456' 的MD5值为 'e10adc3949ba59abbe56e057f20f883e'

UPDATE user SET password = MD5(password) WHERE LENGTH(password) < 32;

-- 4. 如果需要重置特定用户的密码为MD5加密后的 '123456'
-- UPDATE user SET password = MD5('123456') WHERE id = 1;

-- 5. 查看更新后的用户数据
SELECT id, no, name, password, role_id,
       CASE role_id 
           WHEN 0 THEN '系统管理员'
           WHEN 1 THEN '库存管理员'
           WHEN 2 THEN '收银员'
           ELSE '未知角色'
       END as roleName
FROM user
ORDER BY role_id, id;
