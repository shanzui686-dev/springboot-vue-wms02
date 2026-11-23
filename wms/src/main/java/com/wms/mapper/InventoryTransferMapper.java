package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.InventoryTransfer;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 库存调拨表 Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
@Mapper
public interface InventoryTransferMapper extends BaseMapper<InventoryTransfer> {

}
