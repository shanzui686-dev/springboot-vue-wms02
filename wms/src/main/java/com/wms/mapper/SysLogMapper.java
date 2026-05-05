package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SysLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统操作日志Mapper接口
 */
@Mapper
public interface SysLogMapper extends BaseMapper<SysLog> {
}