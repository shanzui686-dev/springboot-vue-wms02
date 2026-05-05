package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.GoodsVO;
import com.wms.mapper.GoodsMapper;
import com.wms.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-03-30
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private IGoodsService goodsService;
    
    @Autowired
    private GoodsMapper goodsMapper;
    //新增
    @Log("新增商品")
    @PostMapping("/save")
    public Result save(@RequestBody Goods goods){
        return goodsService.save(goods)?Result.suc():Result.fail();
    }
    //更新
    @Log("更新商品")
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        return goodsService.updateById(goods)?Result.suc():Result.fail();
    }
    //删除
    @Log("删除商品")
    @GetMapping("/del")
    public Result del(@RequestParam String id){
        return goodsService.removeById(id)?Result.suc():Result.fail();
    }
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query ){
        HashMap param=query.getParam();
        String name=(String) param.get("name");
        Object categoryIdObj = param.get("categoryId");
        Object warehouseIdObj = param.get("warehouseId");
        Object isWarningObj = param.get("isWarning");
        
        String categoryId = categoryIdObj != null ? categoryIdObj.toString() : null;
        String warehouseId = warehouseIdObj != null ? warehouseIdObj.toString() : null;
        Boolean isWarning = isWarningObj != null ? Boolean.parseBoolean(isWarningObj.toString()) : false;
        
        Page<GoodsVO> page=new Page<>();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        
        // 使用连表查询，包含供应商名称、分类名称、仓库名称
        Integer categoryIdInt = StringUtils.isNotBlank(categoryId) && !"null".equals(categoryId) ? Integer.parseInt(categoryId) : null;
        Integer warehouseIdInt = StringUtils.isNotBlank(warehouseId) && !"null".equals(warehouseId) ? Integer.parseInt(warehouseId) : null;
        
        IPage<GoodsVO> result=goodsMapper.selectGoodsWithSupplier(page, name, categoryIdInt, warehouseIdInt, isWarning);
        return Result.suc(result.getRecords(),result.getTotal());
    }

    /**
     * 商品补货建议接口
     * @param purchaseDays 预计采购周期（天），默认7天
     * @return 补货建议列表
     */
    @GetMapping("/suggestRestock")
    public Result suggestRestock(@RequestParam(required = false, defaultValue = "7") Integer purchaseDays){
        return Result.suc(goodsService.suggestRestock(purchaseDays));
    }

    /**
     * 根据条码查询商品信息（收银台扫码使用）
     * @param barcode 商品条码
     * @return 商品列表
     */
    @GetMapping("/list")
    public Result listByBarcode(@RequestParam String barcode){
        if(StringUtils.isBlank(barcode)){
            return Result.fail("条码不能为空");
        }
        // 根据条码精确查询商品
        LambdaQueryWrapper<Goods> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Goods::getBarcode, barcode);
        List<Goods> goodsList = goodsService.list(lambdaQueryWrapper);
        
        if(goodsList.isEmpty()){
            return Result.fail("未找到该条码对应的商品");
        }
        return Result.suc(goodsList);
    }

    /**
     * 根据条码查询单个商品详细信息（收银台专用）
     * @param barcode 商品条码
     * @return 商品详细信息
     */
    @GetMapping("/findByBarcode")
    public Result findByBarcode(@RequestParam String barcode){
        if(StringUtils.isBlank(barcode)){
            return Result.fail("条码不能为空");
        }
        // 根据条码精确查询商品
        LambdaQueryWrapper<Goods> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Goods::getBarcode, barcode);
        Goods goods = goodsService.getOne(lambdaQueryWrapper);
        
        if(goods == null){
            return Result.fail("未找到该条码对应的商品");
        }
        return Result.suc(goods);
    }
}
