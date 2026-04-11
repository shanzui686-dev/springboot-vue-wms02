package com.wms.mapper;

import com.wms.entity.Record;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-04-01
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {
    Page<Record> pageCC(Page page, @Param("ew") Object queryWrapper);
}
