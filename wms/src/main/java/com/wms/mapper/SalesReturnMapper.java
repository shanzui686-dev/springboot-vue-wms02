package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.SalesReturn;
import com.wms.entity.SalesReturnVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 退货单 Mapper 接口
 */
@Mapper
public interface SalesReturnMapper extends BaseMapper<SalesReturn> {

    /**
     * 分页查询退货单列表（连表查询收银员和原销售单信息）
     * @param page 分页对象
     * @param returnNo 退货单号（模糊匹配）
     * @param status 状态
     * @return 分页结果
     */
    IPage<SalesReturnVO> selectReturnPage(Page<SalesReturnVO> page,
                                           @Param("returnNo") String returnNo,
                                           @Param("status") Integer status,
                                           @Param("userId") Integer userId);
}
