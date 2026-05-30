package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.InboundDTO;
import com.wms.entity.Purchase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.PurchaseDTO;
import com.wms.entity.PurchaseVO;

/**
 * <p>
 * 采购主表 服务类
 * </p>
 *
 * @author wms
 * @since 2026-04-20
 */
public interface IPurchaseService extends IService<Purchase> {

    /**
     * 分页查询采购单（连表查询）
     * @param pagenum 页码
     * @param pagesize 每页大小
     * @param purchaseNo 采购单号
     * @param supplierId 供应商ID
     * @param status 状态
     * @return 分页结果
     */
    IPage<PurchaseVO> listPage(Integer pagenum, Integer pagesize, String purchaseNo, Integer supplierId, Integer status);

    /**
     * 保存采购单（包含明细）
     * @param purchaseDTO 采购数据传输对象
     * @return 是否成功
     */
    boolean saveWithDetails(PurchaseDTO purchaseDTO);

    /**
     * 更新采购单（包含明细）
     * @param purchaseDTO 采购数据传输对象
     * @return 是否成功
     */
    boolean updateWithDetails(PurchaseDTO purchaseDTO);

    /**
     * 创建采购单（计算总金额，默认状态为待入库）
     * @param purchaseDTO 采购数据传输对象
     * @return 是否成功
     */
    boolean createPurchase(PurchaseDTO purchaseDTO);

    /**
     * 确认入库（新：需传入仓库和入库数量）
     * @param inboundDTO 入库请求
     * @return 是否成功
     */
    boolean inbound(InboundDTO inboundDTO);

    /**
     * 店长审核采购单（状态 0→1）
     * @param purchaseId 采购单ID
     * @return 是否成功
     */
    boolean audit(Integer purchaseId);

    /**
     * 采购退货（按批次FIFO扣减库存）
     * @param purchaseId 采购单ID
     * @return 是否成功
     */
    boolean returnGoods(Integer purchaseId);

}
