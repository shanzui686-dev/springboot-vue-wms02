package com.wms.service;

import com.wms.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wms
 * @since 2026-04-01
 */
public interface IRecordService extends IService<Record> {

    /**
     * 确认出库记录(管理员审核通过)
     * @param recordId 记录ID
     * @return 是否成功
     */
    boolean confirmRecord(Integer recordId);

    /**
     * 拒绝出库记录
     * @param recordId 记录ID
     * @return 是否成功
     */
    boolean rejectRecord(Integer recordId);
}
