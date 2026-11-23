package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.CheckInputDTO;
import com.wms.entity.InventoryCheck;
import com.wms.entity.InventoryCheckVO;

/**
 * <p>
 * 库存盘点 服务类
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
public interface IInventoryCheckService extends IService<InventoryCheck> {

    /**
     * 创建盘点单（生成单号 -> 插入主表 -> 查询该仓库所有商品 -> 批量插入明细）
     *
     * @param check 盘点单信息
     * @return 是否成功
     */
    boolean createCheck(InventoryCheck check);

    /**
     * 录入实盘数据（更新明细表 -> 自动计算差异 -> 主表状态改为待审核）
     *
     * @param dto 实盘数据
     * @return 是否成功
     */
    boolean inputActualData(CheckInputDTO dto);

    /**
     * 审核盘点单（状态改为已完成 -> 遍历明细调整商品库存 -> 插入流水记录）
     *
     * @param checkId 盘点单ID
     * @return 是否成功
     */
    boolean auditCheck(Integer checkId);

    /**
     * 分页查询盘点单（连表查询仓库名称）
     *
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param checkNo 单号（模糊）
     * @param status 状态
     * @return 分页结果
     */
    IPage<InventoryCheckVO> listPage(Integer pageNum, Integer pageSize, String checkNo, Integer status);
}
