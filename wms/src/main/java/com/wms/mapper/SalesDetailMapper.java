package com.wms.mapper;

import com.wms.entity.SalesDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SalesDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 销售明细 Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@Mapper
public interface SalesDetailMapper extends BaseMapper<SalesDetail> {

    /**
     * 查询销售明细列表（连表查询商品名称）
     * @param salesId 销售单ID
     * @return 销售明细列表
     */
    List<SalesDetailVO> selectDetailsBySalesId(@Param("salesId") Integer salesId);
}
