package com.wms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作人
     */
    private String username;

    /**
     * 操作描述
     */
    private String operation;

    /**
     * 请求方法全路径
     */
    private String method;

    /**
     * 请求参数JSON
     */
    private String params;

    /**
     * 操作IP
     */
    private String ip;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 执行耗时(ms)
     */
    private Long executionTime;
}