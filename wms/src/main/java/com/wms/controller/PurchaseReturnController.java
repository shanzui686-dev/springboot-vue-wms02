package com.wms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.RequireRole;
import com.wms.common.Result;
import com.wms.entity.PurchaseReturn;
import com.wms.entity.PurchaseReturnDTO;
import com.wms.service.IPurchaseReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/purchaseReturn")
@RequireRole({0, 1}) // 店长+库存管理员可处理采购退回
public class PurchaseReturnController {

    @Autowired
    private IPurchaseReturnService purchaseReturnService;

    /** 采购员提交退货申请 */
    @Log("提交采购退货申请")
    @PostMapping("/submit")
    public Result submit(@RequestBody PurchaseReturnDTO dto) {
        try {
            PurchaseReturn pr = purchaseReturnService.submit(dto);
            return Result.suc(pr);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 店长审核通过 → 扣库存 */
    @Log("审核通过采购退货")
    @PostMapping("/approve")
    public Result approve(@RequestParam Integer returnId, @RequestParam Integer auditorId) {
        try {
            return purchaseReturnService.approve(returnId, auditorId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 店长审核拒绝 */
    @Log("审核拒绝采购退货")
    @PostMapping("/reject")
    public Result reject(@RequestParam Integer returnId, @RequestParam Integer auditorId) {
        try {
            return purchaseReturnService.reject(returnId, auditorId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /** 分页查询退货单列表 */
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String returnNo = (String) param.get("returnNo");
        Object statusObj = param.get("status");
        Integer status = statusObj != null ? Integer.parseInt(statusObj.toString()) : null;
        IPage<PurchaseReturn> result = purchaseReturnService.listPage(
            query.getPagenum(), query.getPagesize(), returnNo, status);
        return Result.suc(result.getRecords(), result.getTotal());
    }
}