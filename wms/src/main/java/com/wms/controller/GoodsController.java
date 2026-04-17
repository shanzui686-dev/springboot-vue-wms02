package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;

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
    //新增
    @PostMapping("/save")
    public Result save(@RequestBody Goods goods){
        return goodsService.save(goods)?Result.suc():Result.fail();
    }
    //更新
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        return goodsService.updateById(goods)?Result.suc():Result.fail();
    }
    //删除
    @GetMapping("/del")
    public Result del(@RequestParam String id){
        return goodsService.removeById(id)?Result.suc():Result.fail();
    }
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query ){
        HashMap param=query.getParam();
        String name=(String) param.get("name");
        Object goodstypeObj = param.get("goodstype");
        Object storageObj = param.get("storage");
        String goodstype = goodstypeObj != null ? goodstypeObj.toString() : null;
        String storage = storageObj != null ? storageObj.toString() : null;
        Page<Goods> page=new Page();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        LambdaQueryWrapper<Goods> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(Goods::getName,name);
        }
        if(StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)){
            lambdaQueryWrapper.eq(Goods::getGoodsType,Integer.parseInt(goodstype));
        }
        if(StringUtils.isNotBlank(storage) && !"null".equals(storage)){
            lambdaQueryWrapper.eq(Goods::getStorage,Integer.parseInt(storage));
        }
        IPage result=goodsService.page(page,lambdaQueryWrapper);
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
}
