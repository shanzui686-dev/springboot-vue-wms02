package com.wms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Log;
import com.wms.common.RequireRole;
import com.wms.common.Result;
import com.wms.entity.SalesDTO;
import com.wms.entity.SalesDetailVO;
import com.wms.entity.SalesVO;
import com.wms.service.ISalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 销售单前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@RestController
@RequestMapping("/sales")
@RequireRole({0, 2}) // 店长+收银员
public class SalesController {

    @Autowired
    private ISalesService salesService;

    /**
     * 收银结算接口
     * @param salesDTO 销售单数据传输对象
     * @return 结算结果，包含订单流水号
     */
    @Log("销售结算")
    @PostMapping("/checkout")
    public Result checkout(@RequestBody SalesDTO salesDTO) {
        try {
            Integer orderId = salesService.checkout(salesDTO);
            return Result.suc(orderId).msg("结算成功");
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("结算失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询销售单列表
     * @param page 当前页码
     * @param limit 每页条数
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果，包含销售单列表和收银员姓名
     */
    @GetMapping("/listPage")
    public Result listPage(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer limit,
                           @RequestParam(required = false) String orderNum,
                           @RequestParam(required = false) String startDate,
                           @RequestParam(required = false) String endDate,
                           @RequestParam(required = false) Integer userId) {
        Page<SalesVO> pageParam = new Page<>(page, limit);
        IPage<SalesVO> result = salesService.listPage(pageParam, orderNum, startDate, endDate, userId);
        return Result.suc(result);
    }

    /**
     * 查询销售明细列表
     * @param salesId 销售单ID
     * @return 销售明细列表，包含商品名称、单价、数量和小计
     */
    @GetMapping("/getDetails")
    public Result getDetails(@RequestParam Integer salesId) {
        // 调用服务层方法，查询销售明细
        List<SalesDetailVO> details = salesService.getDetails(salesId);
        
        // 返回查询结果
        return Result.suc(details);
    }

    /**
     * 查询销售汇总（销售笔数 / 应收总额 / 实收总额）
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 汇总结果
     */
    @GetMapping("/summary")
    public Result summary(@RequestParam(required = false) String orderNum,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          @RequestParam(required = false) Integer userId) {
        return Result.suc(salesService.summary(orderNum, startDate, endDate, userId));
    }

    /**
     * 查询退换货汇总（退货笔数 / 应退款金额 / 实退款金额）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 退换货汇总
     */
    @GetMapping("/returnSummary")
    public Result returnSummary(@RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @RequestParam(required = false) Integer userId) {
        return Result.suc(salesService.returnSummary(startDate, endDate, userId));
    }

    /**
     * 查询商品销售汇总（饼图数据：销售了哪些商品）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 商品销售汇总列表
     */
    @GetMapping("/goodsSales")
    public Result goodsSales(@RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate,
                             @RequestParam(required = false) Integer userId) {
        return Result.suc(salesService.goodsSales(startDate, endDate, userId));
    }

    /**
     * 查询退换货商品汇总（饼图数据：退换货数量占比 / 退款金额占比）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param userId 收银员ID
     * @return 退换货商品汇总列表
     */
    @GetMapping("/returnGoodsSales")
    public Result returnGoodsSales(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) Integer userId) {
        return Result.suc(salesService.returnGoodsSales(startDate, endDate, userId));
    }
}

