-- 修改menu表中的"物品管理"为"商品管理"
UPDATE menu SET menuName = '商品管理' WHERE menuCode = '005' AND menuName = '物品管理';

-- 修改menu表中的"物品分类管理"为"商品分类管理"
UPDATE menu SET menuName = '商品分类管理' WHERE menuCode = '004' AND menuName = '物品分类管理';

-- 验证修改结果
SELECT id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight, menuComponent, menuIcon 
FROM menu 
WHERE menuCode IN ('004', '005')
ORDER BY menuCode;
