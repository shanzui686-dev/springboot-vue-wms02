-- 操作日志表
CREATE TABLE sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '操作人',
    operation VARCHAR(200) NOT NULL COMMENT '操作描述',
    method VARCHAR(200) NOT NULL COMMENT '请求方法全路径',
    params TEXT COMMENT '请求参数JSON',
    ip VARCHAR(50) COMMENT '操作IP',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    execution_time BIGINT NOT NULL DEFAULT 0 COMMENT '执行耗时(ms)',
    INDEX idx_username (username),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';