-- 损耗管理模块 — 损耗单主表
CREATE TABLE loss_report (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    loss_no         VARCHAR(50) NOT NULL COMMENT '损耗单号 LOSS+年月日+随机',
    goods_id        INT NOT NULL COMMENT '商品ID',
    batch_id        INT COMMENT 'goods_batch ID',
    batch_no        VARCHAR(50) COMMENT '批次号',
    loss_count      INT NOT NULL COMMENT '损耗数量',
    loss_type       INT DEFAULT 1 COMMENT '1:库存损耗 2:销售损耗',
    purchase_price  DECIMAL(10,2) COMMENT '进价快照',
    loss_amount     DECIMAL(10,2) COMMENT '损耗金额=进价×数量',
    reason          VARCHAR(500) COMMENT '损耗原因',
    status          INT DEFAULT 0 COMMENT '0:待审核 1:已处理 2:已拒绝',
    reporter_id     INT COMMENT '上报人ID',
    auditor_id      INT COMMENT '审核人ID',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    audit_time      DATETIME,
    INDEX idx_loss_status (status),
    INDEX idx_loss_goods (goods_id)
);
