package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.SysLog;

/**
 * 系统操作日志Service接口
 */
public interface ISysLogService {
    
    /**
     * 保存日志
     */
    void saveLog(SysLog sysLog);
    
    /**
     * 分页查询日志
     */
    IPage<SysLog> pageList(Page<SysLog> page, String username, String startTime, String endTime);
}