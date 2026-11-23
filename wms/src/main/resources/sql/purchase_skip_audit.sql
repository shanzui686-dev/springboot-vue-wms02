-- 采购免审核改造（2026-11-15）：采购单创建后直接为"待入库"
-- 将历史"待审核"(status=0) 的采购单迁移为"待入库"(status=1)，使其可直接确认入库
UPDATE purchase SET status = 1 WHERE status = 0;
