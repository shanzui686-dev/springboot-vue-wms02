-- ============================================
-- 添加采购退回审核菜单
-- 执行时间: 2026-10-19
-- ============================================

-- 添加采购退回审核菜单（仅店长可见）
INSERT INTO menu (id, menuCode, menuName, menuLevel, menuParentCode, menuClick, menuRight, menuComponent, menuIcon) VALUES
(33, 'purrview', '采购退回审核', '2', 'purchase', '/PurchaseReturnReview', '0', 'purchase/PurchaseReturnReview', 'RefreshLeft');

-- 验证添加结果
SELECT menuCode, menuName, menuRight FROM menu WHERE menuCode = 'purrview';