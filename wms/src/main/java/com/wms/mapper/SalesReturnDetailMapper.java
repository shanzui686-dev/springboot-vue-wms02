package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.ReturnDetailVO;
import com.wms.entity.SalesReturnDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 退货明细 Mapper 接口
 */
@Mapper
public interface SalesReturnDetailMapper extends BaseMapper<SalesReturnDetail> {

    /**
     * 查询退货明细列表（连表查询商品名称）
     * @param returnId 退货单ID
     * @return 退货明细列表
     */
    List<ReturnDetailVO> selectDetailsByReturnId(@Param("returnId") Integer returnId);
}
