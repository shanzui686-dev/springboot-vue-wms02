-- ============================================
-- 库存盘点与库存调拨测试数据
-- ============================================

-- ============================================
-- 1. 库存盘点主表测试数据
-- ============================================
-- 盘点单1：2026年5月1日，管理员(sa)对仓库1进行盘点，状态：已完成
INSERT INTO inventory_check (check_no, storage_id, status, remark, create_time, creator_id) 
VALUES ('PD202605010001', 1, 2, '月度例行盘点', '2026-05-01 10:30:00', 1);

-- 盘点单2：2026年5月3日，管理员(sa)对仓库2进行盘点，状态：待审核
INSERT INTO inventory_check (check_no, storage_id, status, remark, create_time, creator_id) 
VALUES ('PD202605030001', 2, 1, '季度盘点', '2026-05-03 14:20:00', 1);

-- 盘点单3：2026年5月7日，库存管理员(manager)对仓库3进行盘点，状态：待录入
INSERT INTO inventory_check (check_no, storage_id, status, remark, create_time, creator_id) 
VALUES ('PD202605070001', 3, 0, '临时盘点', '2026-05-07 09:15:00', 2);

-- 盘点单4：2026年5月8日，管理员(sa)对仓库4进行盘点，状态：已完成
INSERT INTO inventory_check (check_no, storage_id, status, remark, create_time, creator_id) 
VALUES ('PD202605080001', 4, 2, '年终盘点', '2026-05-08 16:45:00', 1);

-- ============================================
-- 2. 库存盘点明细表测试数据
-- ============================================

-- 盘点单1 (check_id=1) 的明细：仓库1，盘点5个商品
-- 商品1：怡宝纯净水，账面1000，实盘985，差异-15（少了15个）
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (1, 1, 1000, 985, -15, '可能是破损未记录');

-- 商品2：蓝月亮洗衣液，账面500，实盘512，差异+12（多了12个）
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (1, 2, 500, 512, 12, '供应商多发了货');

-- 商品3：伊利纯牛奶，账面800，实盘800，差异0（无差异）
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (1, 3, 800, 800, 0, NULL);

-- 商品4：海天酱油，账面600，实盘590，差异-10
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (1, 4, 600, 590, -10, '可能是过期下架');

-- 商品5：金龙鱼食用油，账面400，实盘405，差异+5
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (1, 5, 400, 405, 5, '退货未处理');

-- 盘点单2 (check_id=2) 的明细：仓库2，盘点3个商品
-- 商品6：可口可乐，账面1000，实盘1020，差异+20
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (2, 6, 1000, 1020, 20, '促销活动赠品未扣除');

-- 商品7：雪碧，账面800，实盘785，差异-15
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (2, 7, 800, 785, -15, '运输损耗');

-- 商品8：矿泉水，账面2000，实盘2000，差异0
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (2, 8, 2000, 2000, 0, NULL);

-- 盘点单3 (check_id=3) 的明细：仓库3，盘点2个商品（待录入状态，尚未填写实盘数量）
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (3, 11, 350, NULL, NULL, '待盘点');

INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (3, 12, 280, NULL, NULL, '待盘点');

-- 盘点单4 (check_id=4) 的明细：仓库4，盘点4个商品
-- 商品16：鼠标，账面180，实盘175，差异-5
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (4, 16, 180, 175, -5, '样品展示损耗');

-- 商品17：键盘，账面120，实盘125，差异+5
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (4, 17, 120, 125, 5, '客户退货入库');

-- 商品18：耳机，账面200，实盘198，差异-2
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (4, 18, 200, 198, -2, '破损下架');

-- 商品19：U盘，账面300，实盘300，差异0
INSERT INTO inventory_check_detail (check_id, goods_id, expected_count, actual_count, diff_count, remark) 
VALUES (4, 19, 300, 300, 0, NULL);

-- ============================================
-- 3. 库存调拨表测试数据
-- ============================================

-- 调拨单1：从仓库1调拨到仓库2
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605020001', 1, 2, 1, 50, '2026-05-02 09:30:00', 1, '仓库2库存紧张，补充怡宝纯净水');

-- 调拨单2：从仓库1调拨到仓库3
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605020002', 1, 3, 2, 30, '2026-05-02 10:15:00', 1, '仓库3促销，调拨蓝月亮洗衣液');

-- 调拨单3：从仓库2调拨到仓库1
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605040001', 2, 1, 6, 100, '2026-05-04 14:20:00', 2, '仓库1可口可乐库存不足');

-- 调拨单4：从仓库2调拨到仓库4
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605050001', 2, 4, 8, 200, '2026-05-05 11:00:00', 1, '仓库4新开业，调拨矿泉水');

-- 调拨单5：从仓库3调拨到仓库1
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605060001', 3, 1, 11, 20, '2026-05-06 16:30:00', 2, '仓库1洗发水库存不足');

-- 调拨单6：从仓库4调拨到仓库2
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605090001', 4, 2, 16, 10, '2026-05-09 09:45:00', 1, '仓库2电子产品促销');

-- 调拨单7：从仓库1调拨到仓库4
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605090002', 1, 4, 3, 50, '2026-05-09 10:20:00', 2, '仓库4伊利纯牛奶补货');

-- 调拨单8：从仓库3调拨到仓库2
INSERT INTO inventory_transfer (transfer_no, from_storage_id, to_storage_id, goods_id, count, create_time, creator_id, remark) 
VALUES ('DB202605100001', 3, 2, 13, 40, '2026-05-10 08:30:00', 1, '仓库2牙膏库存预警');

-- ============================================
-- 完成提示
-- ============================================
-- 测试数据生成完成！
-- 包含以下数据：
-- 1. 库存盘点主表: 4条记录 (2个已完成，1个待审核，1个待录入)
-- 2. 库存盘点明细表: 14条记录 (覆盖不同差异情况：正差异、负差异、无差异)
-- 3. 库存调拨表: 8条记录 (不同仓库之间的商品调拨)
-- ============================================

-- 验证数据
SELECT '盘点主表' as table_name, COUNT(*) as count FROM inventory_check
UNION ALL
SELECT '盘点明细表', COUNT(*) FROM inventory_check_detail
UNION ALL
SELECT '调拨表', COUNT(*) FROM inventory_transfer;
