package com.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Sales;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.ProductSalesVO;
import com.wms.entity.SalesVO;
import com.wms.entity.SalesSummaryVO;

import java.util.List;
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

    /**
     * 查询销售汇总（销售笔数 / 应收总额 / 实收总额，与 selectSalesPage 同口径）
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 销售汇总
     */
    SalesSummaryVO selectSalesSummary(@Param("orderNum") String orderNum,
                                      @Param("startDate") String startDate,
                                      @Param("endDate") String endDate,
                                      @Param("userId") Integer userId);

    /**
     * 查询退换货汇总（退货笔数 / 应退款金额 / 实退款金额）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 退换货汇总
     */
    SalesSummaryVO selectReturnSummary(@Param("startDate") String startDate,
                                       @Param("endDate") String endDate,
                                       @Param("userId") Integer userId);

    /**
     * 查询商品销售汇总（饼图数据：销售了哪些商品）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 商品销售汇总列表
     */
    List<ProductSalesVO> selectGoodsSales(@Param("startDate") String startDate,
                                          @Param("endDate") String endDate,
                                          @Param("userId") Integer userId);
}
