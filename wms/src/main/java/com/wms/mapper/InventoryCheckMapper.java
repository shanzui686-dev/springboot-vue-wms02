package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.InventoryCheck;
import com.wms.entity.InventoryCheckVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 库存盘点主表 Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
@Mapper
public interface InventoryCheckMapper extends BaseMapper<InventoryCheck> {

    /**
     * 分页查询盘点单（连表查询仓库名称）
     *
     * @param page 分页对象
     * @param checkNo 单号（模糊）
     * @param status 状态
     * @return 分页结果
     */
    IPage<InventoryCheckVO> selectCheckPage(IPage<InventoryCheckVO> page, @Param("checkNo") String checkNo, @Param("status") Integer status);
}
