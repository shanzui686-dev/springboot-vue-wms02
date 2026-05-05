-- 在 menu 表中插入"操作日志"菜单项
-- 使用系统管理图标，路径为 OperationLog
-- 注意：menuCode 使用短编码避免字段长度问题

INSERT INTO menu (menuName, menuIcon, menuClick, menuComponent, menuRight, menuLevel, menuParentCode, menuCode) 
VALUES ('操作日志', 'el-icon-s-management', 'OperationLog', 'system/OperationLog.vue', '0', '1', '0', 'oplog');

-- 注意：
-- menuName: 菜单显示名称
-- menuIcon: 菜单图标（使用 el-icon-s-management 设置图标）
-- menuClick: 路由路径（对应 router/index.js 中的 path）
-- menuComponent: 组件路径（相对于 components 目录）
-- menuRight: 权限标识（0表示所有角色可见）
-- menuLevel: 菜单层级（1表示一级菜单）
-- menuParentCode: 父级菜单编码（0表示顶级菜单）
-- menuCode: 菜单编码标识
