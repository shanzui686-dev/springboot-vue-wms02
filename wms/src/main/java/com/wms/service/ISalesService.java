package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Sales;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.SalesDTO;
import com.wms.entity.SalesDetailVO;
import com.wms.entity.SalesVO;

import java.util.List;

/**
 * <p>
 * 销售单服务类
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
public interface ISalesService extends IService<Sales> {

    /**
     * 收银结算
     * @param salesDTO 销售单数据传输对象
     * @return 订单ID（流水号）
     */
    Integer checkout(SalesDTO salesDTO);

    /**
     * 分页查询销售单列表
     * @param page 分页对象
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    IPage<SalesVO> listPage(Page<SalesVO> page, String orderNum, String startDate, String endDate);

    /**
     * 查询销售明细列表
     * @param salesId 销售单ID
     * @return 销售明细列表
     */
    List<SalesDetailVO> getDetails(Integer salesId);

    /**
     * 收银台直接退款（回滚库存）
     * @param orderNum 销售单流水号
     * @return 是否成功
     */
    Boolean refund(String orderNum);
}
