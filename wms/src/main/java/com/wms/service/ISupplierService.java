package com.wms.service;

import com.wms.entity.Supplier;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 供应商服务类
 * </p>
 *
 * @author wms
 * @since 2026-04-17
 */
public interface ISupplierService extends IService<Supplier> {

    /**
     * 更新供应商状态
     * @param id 供应商ID
     * @param status 状态：1启用，0禁用
     * @return 是否成功
     */
    boolean updateStatus(Integer id, Integer status);
}
