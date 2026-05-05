package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.ReturnApplyDTO;
import com.wms.entity.ReturnDetailVO;
import com.wms.entity.SalesReturn;
import com.wms.entity.SalesReturnVO;

import java.util.List;

/**
 * 退货单服务类
 */
public interface ISalesReturnService extends IService<SalesReturn> {

    /**
     * 发起退货申请
     * @param returnApplyDTO 退货申请DTO
     * @return 退货单ID
     */
    Integer applyReturn(ReturnApplyDTO returnApplyDTO);

    /**
     * 确认退款并回滚库存
     * @param returnId 退货单ID
     * @return 是否成功
     */
    Boolean confirmRefund(Integer returnId);

    /**
     * 分页查询退货单列表
     * @param page 分页对象
     * @param returnNo 退货单号
     * @param status 状态
     * @return 分页结果
     */
    IPage<SalesReturnVO> listPage(Page<SalesReturnVO> page, String returnNo, Integer status);

    /**
     * 查询退货明细列表
     * @param returnId 退货单ID
     * @return 退货明细列表
     */
    List<ReturnDetailVO> getDetails(Integer returnId);
}
