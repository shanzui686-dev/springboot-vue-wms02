package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.PurchaseReturn;
import com.wms.entity.PurchaseReturnDTO;

public interface IPurchaseReturnService extends IService<PurchaseReturn> {

    /** 采购员提交退货申请（新建 → 待审核） */
    PurchaseReturn submit(PurchaseReturnDTO dto);

    /** 店长审核通过 → 扣减 goods_batch + 更新库存 */
    boolean approve(Integer returnId, Integer auditorId);

    /** 店长拒绝 */
    boolean reject(Integer returnId, Integer auditorId);

    /** 分页查询退货单列表 */
    IPage<PurchaseReturn> listPage(Integer pagenum, Integer pagesize, String returnNo, Integer status);
}