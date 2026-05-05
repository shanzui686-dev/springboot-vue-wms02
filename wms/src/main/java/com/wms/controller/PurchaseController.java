package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.PurchaseDTO;
import com.wms.entity.PurchaseDetail;
import com.wms.entity.PurchaseVO;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.PurchaseDetailMapper;
import com.wms.service.IPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购主表 前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-04-20
 */
@RestController
@RequestMapping("/purchase")
public class PurchaseController {

    @Autowired
    private IPurchaseService purchaseService;

    @Autowired
    private PurchaseDetailMapper purchaseDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 分页查询采购单
     */
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        
        String purchaseNo = (String) param.get("purchaseNo");
        Object supplierIdObj = param.get("supplierId");
        Object statusObj = param.get("status");
        
        Integer supplierId = supplierIdObj != null ? Integer.parseInt(supplierIdObj.toString()) : null;
        Integer status = statusObj != null ? Integer.parseInt(statusObj.toString()) : null;

        IPage<PurchaseVO> result = purchaseService.listPage(
            query.getPagenum(),
            query.getPagesize(),
            purchaseNo,
            supplierId,
            status
        );

        return Result.suc(result.getRecords(), result.getTotal());
    }

    /**
     * 保存采购单（包含明细）
     */
    @Log("保存采购单")
    @PostMapping("/save")
    public Result save(@RequestBody PurchaseDTO purchaseDTO) {
        return purchaseService.saveWithDetails(purchaseDTO) ? Result.suc() : Result.fail();
    }

    /**
     * 更新采购单（包含明细）
     */
    @Log("更新采购单")
    @PostMapping("/update")
    public Result update(@RequestBody PurchaseDTO purchaseDTO) {
        return purchaseService.updateWithDetails(purchaseDTO) ? Result.suc() : Result.fail();
    }

    /**
     * 删除采购单
     */
    @Log("删除采购单")
    @GetMapping("/del")
    public Result del(@RequestParam String id) {
        return purchaseService.removeById(id) ? Result.suc() : Result.fail();
    }

    /**
     * 创建采购单（计算总金额，默认状态为待入库）
     */
    @Log("创建采购单")
    @PostMapping("/create")
    public Result create(@RequestBody PurchaseDTO purchaseDTO) {
        System.out.println("接收到的采购数据: " + purchaseDTO);
        try {
            return purchaseService.createPurchase(purchaseDTO) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("创建失败：" + e.getMessage());
        }
    }

    /**
     * 确认入库（更新状态为已入库，增加商品库存）
     */
    @Log("采购入库")
    @PostMapping("/inbound")
    public Result inbound(@RequestParam Integer purchaseId) {
        try {
            return purchaseService.inbound(purchaseId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 采购退货（更新状态为已退货，扣减商品库存）
     */
    @Log("采购退货")
    @PostMapping("/returnGoods")
    public Result returnGoods(@RequestParam Integer purchaseId) {
        try {
            return purchaseService.returnGoods(purchaseId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 查询采购单明细列表
     */
    @GetMapping("/detail/list")
    public Result getDetailList(@RequestParam Integer purchaseId) {
        // 查询采购明细
        LambdaQueryWrapper<PurchaseDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetail::getPurchaseId, purchaseId);
        List<PurchaseDetail> details = purchaseDetailMapper.selectList(queryWrapper);

        // 关联查询商品信息，封装返回数据
        List<Map<String, Object>> resultList = details.stream().map(detail -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", detail.getId());
            map.put("purchaseId", detail.getPurchaseId());
            map.put("goodsId", detail.getGoodsId());
            map.put("count", detail.getCount());
            map.put("price", detail.getPrice());
            map.put("subtotal", detail.getSubtotal());
            map.put("remark", detail.getRemark());

            // 查询商品信息
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods != null) {
                map.put("goodsName", goods.getName());
                map.put("barcode", goods.getBarcode());
                map.put("specs", goods.getSpecs());
                map.put("unit", goods.getUnit());
            } else {
                map.put("goodsName", "未知商品");
            }

            return map;
        }).collect(Collectors.toList());

        return Result.suc(resultList);
    }
}
