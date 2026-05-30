package com.wms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Log;
import com.wms.common.Result;
import com.wms.entity.ReturnApplyDTO;
import com.wms.entity.ReturnDetailVO;
import com.wms.entity.SalesReturnVO;
import com.wms.service.ISalesReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 退货管理控制器
 */
@RestController
@RequestMapping("/return")
public class SalesReturnController {

    @Autowired
    private ISalesReturnService salesReturnService;

    /**
     * 发起退货申请
     * @param returnApplyDTO 退货申请DTO
     * @return 退货单ID
     */
    @Log("发起退货申请")
    @PostMapping("/apply")
    public Result applyReturn(@RequestBody ReturnApplyDTO returnApplyDTO) {
        try {
            Integer returnId = salesReturnService.applyReturn(returnApplyDTO);
            return Result.suc(returnId).msg("退货申请成功");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("退货申请失败：" + e.getMessage());
        }
    }

    /**
     * 确认退款并回滚库存
     * @param returnId 退货单ID
     * @return 操作结果
     */
    @Log("确认退款")
    @PostMapping("/confirm")
    public Result confirmRefund(@RequestParam Integer returnId) {
        try {
            Boolean result = salesReturnService.confirmRefund(returnId);
            if (result) {
                return Result.suc().msg("确认退款成功，库存已回滚");
            } else {
                return Result.fail("确认退款失败");
            }
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("确认退款失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询退货单列表
     * @param page 当前页码
     * @param limit 每页条数
     * @param returnNo 退货单号（模糊匹配）
     * @param status 状态（0待退款，1已退款）
     * @return 分页结果
     */
    @GetMapping("/listPage")
    public Result listPage(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer limit,
                           @RequestParam(required = false) String returnNo,
                           @RequestParam(required = false) Integer status,
                           @RequestParam(required = false) Integer userId) {
        Page<SalesReturnVO> pageParam = new Page<>(page, limit);
        IPage<SalesReturnVO> result = salesReturnService.listPage(pageParam, returnNo, status, userId);
        return Result.suc(result);
    }

    /**
     * 查询退货明细列表
     * @param returnId 退货单ID
     * @return 退货明细列表，包含商品名称、单价、数量和小计
     */
    @GetMapping("/getDetails")
    public Result getDetails(@RequestParam Integer returnId) {
        // 调用服务层方法，查询退货明细
        List<ReturnDetailVO> details = salesReturnService.getDetails(returnId);
        
        // 返回查询结果
        return Result.suc(details);
    }
}
