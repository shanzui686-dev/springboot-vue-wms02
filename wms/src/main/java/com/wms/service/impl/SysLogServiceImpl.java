package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.SysLog;
import com.wms.mapper.SysLogMapper;
import com.wms.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 系统操作日志Service实现类
 */
@Service
public class SysLogServiceImpl implements ISysLogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    @Override
    public void saveLog(SysLog sysLog) {
        sysLogMapper.insert(sysLog);
    }

    @Override
    public IPage<SysLog> pageList(Page<SysLog> page, String username, String startTime, String endTime) {
        LambdaQueryWrapper<SysLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 按操作人查询
        if (StringUtils.hasText(username)) {
            queryWrapper.like(SysLog::getUsername, username);
        }
        
        // 按时间范围查询
        if (StringUtils.hasText(startTime)) {
            queryWrapper.ge(SysLog::getCreateTime, startTime);
        }
        if (StringUtils.hasText(endTime)) {
            queryWrapper.le(SysLog::getCreateTime, endTime);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysLog::getCreateTime);
        
        return sysLogMapper.selectPage(page, queryWrapper);
    }
}