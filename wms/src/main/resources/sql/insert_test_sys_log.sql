-- 插入测试操作日志数据
INSERT INTO sys_log (username, operation, method, params, ip, create_time, execution_time) 
VALUES 
('超级管理员', '新增商品', 'com.wms.controller.GoodsController.save', '[{"name":"测试商品","barcode":"123456","count":100}]', '127.0.0.1', NOW(), 45),
('超级管理员', '更新商品', 'com.wms.controller.GoodsController.update', '[{"id":1,"name":"测试商品","count":150}]', '127.0.0.1', NOW(), 32),
('超级管理员', '删除商品', 'com.wms.controller.GoodsController.del', '[{"id":"2"}]', '127.0.0.1', NOW(), 28);

-- 查询验证
SELECT * FROM sys_log ORDER BY create_time DESC;
