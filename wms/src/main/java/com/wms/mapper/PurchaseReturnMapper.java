package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.PurchaseReturn;
import com.wms.entity.PurchaseReturnVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseReturnMapper extends BaseMapper<PurchaseReturn> {

    Page<PurchaseReturnVO> selectPage(Page<PurchaseReturn> page,
                                      @Param("returnNo") String returnNo,
                                      @Param("status") Integer status);
}