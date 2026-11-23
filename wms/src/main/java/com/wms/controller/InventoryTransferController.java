package com.wms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.RequireRole;
import com.wms.common.Result;
import com.wms.entity.InventoryTransfer;
import com.wms.entity.TransferDTO;
import com.wms.entity.TransferVO;
import com.wms.service.IInventoryTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/inventoryTransfer")
@RequireRole({0, 1}) // 店长+库存管理员可进行库存调拨
public class InventoryTransferController {

    @Autowired
    private IInventoryTransferService inventoryTransferService;

    /**
     * 创建调拨申请（status=0 待审核）
     */
    @Log("创建调拨申请")
    @PostMapping("/create")
    public Result create(@RequestBody TransferDTO dto) {
        try {
            return inventoryTransferService.createTransfer(dto) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核通过（执行实际库存调拨）
     */
    @Log("调拨审核通过")
    @PostMapping("/audit")
    public Result audit(@RequestParam Integer transferId, @RequestParam Integer auditorId) {
        try {
            return inventoryTransferService.auditTransfer(transferId, auditorId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核拒绝
     */
    @Log("调拨审核拒绝")
    @PostMapping("/reject")
    public Result reject(@RequestParam Integer transferId, @RequestParam Integer auditorId) {
        try {
            return inventoryTransferService.rejectTransfer(transferId, auditorId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 分页查询调拨记录
     */
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();

        String transferNo = (String) param.get("transferNo");
        Integer goodsId = null;
        Integer status = null;
        if (param.get("goodsId") != null) {
            goodsId = Integer.parseInt(param.get("goodsId").toString());
        }
        if (param.get("status") != null) {
            status = Integer.parseInt(param.get("status").toString());
        }

        IPage<TransferVO> result = inventoryTransferService.listPage(
            query.getPagenum(),
            query.getPagesize(),
            transferNo,
            goodsId,
            status
        );

        return Result.suc(result.getRecords(), result.getTotal());
    }
}
