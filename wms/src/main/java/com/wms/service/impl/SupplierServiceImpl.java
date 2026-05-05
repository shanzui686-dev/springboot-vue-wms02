package com.wms.service.impl;

import com.wms.entity.Supplier;
import com.wms.mapper.SupplierMapper;
import com.wms.service.ISupplierService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 供应商服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-04-17
 */
@Service
public class SupplierServiceImpl extends ServiceImpl<SupplierMapper, Supplier> implements ISupplierService {

    /**
     * 更新供应商状态
     * @param id 供应商ID
     * @param status 状态：1启用，0禁用
     * @return 是否成功
     */
    @Override
    public boolean updateStatus(Integer id, Integer status) {
        Supplier supplier = new Supplier();
        supplier.setId(id);
        supplier.setStatus(status);
        return this.updateById(supplier);
    }
}
