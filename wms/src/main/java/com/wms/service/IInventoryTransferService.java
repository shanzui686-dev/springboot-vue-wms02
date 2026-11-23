package com.wms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.InventoryTransfer;
import com.wms.entity.TransferDTO;
import com.wms.entity.TransferVO;

public interface IInventoryTransferService extends IService<InventoryTransfer> {

    boolean createTransfer(TransferDTO dto);

    boolean auditTransfer(Integer transferId, Integer auditorId);

    boolean rejectTransfer(Integer transferId, Integer auditorId);

    IPage<TransferVO> listPage(Integer pageNum, Integer pageSize, String transferNo, Integer goodsId, Integer status);
}
