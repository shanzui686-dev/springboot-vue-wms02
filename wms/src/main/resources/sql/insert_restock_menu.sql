-- 在 menu 表中插入"智能补货建议"菜单项
-- 数据库字段使用驼峰命名（menuName, menuIcon 等）

INSERT INTO menu (menuName, menuIcon, menuClick, menuComponent, menuRight, menuLevel, menuParentCode, menuCode) 
VALUES ('智能补货建议', 'el-icon-s-order', 'RestockSuggest', 'stats/RestockSuggest.vue', '0,1', '1', '0', 'restock');

-- 字段说明：
-- menuName: 菜单显示名称
-- menuIcon: 菜单图标（el-icon-s-order 管理图标）
-- menuClick: 路由路径（对应组件文件名，不含.vue）
-- menuComponent: 组件路径（相对于 components 目录）
-- menuRight: 权限标识（0表示所有角色可见）
-- menuLevel: 菜单层级（1表示一级菜单）
-- menuParentCode: 父级菜单编码（0表示顶级菜单）
-- menuCode: 菜单编码标识（简短英文）
