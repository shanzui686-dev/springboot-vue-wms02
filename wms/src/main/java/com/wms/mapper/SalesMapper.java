package com.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Sales;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.SalesVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 销售单 Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@Mapper
public interface SalesMapper extends BaseMapper<Sales> {

    /**
     * 分页查询销售单列表（连表查询收银员姓名）
     * @param page 分页对象
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    IPage<SalesVO> selectSalesPage(Page<SalesVO> page,
                                    @Param("orderNum") String orderNum,
                                    @Param("startDate") String startDate,
                                    @Param("endDate") String endDate,
                                    @Param("userId") Integer userId);
}
