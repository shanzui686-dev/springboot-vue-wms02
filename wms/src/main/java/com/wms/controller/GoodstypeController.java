package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goodstype;
import com.wms.service.IGoodstypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-03-29
 */
@RestController
@RequestMapping("/goodstype")
public class GoodstypeController {
    @Autowired
    private IGoodstypeService goodstypeService;
    //新增
    @PostMapping("/save")
    public Result save(@RequestBody Goodstype goodstype){
        return goodstypeService.save(goodstype)?Result.suc():Result.fail();
    }
    //更新
    @PostMapping("/update")
    public Result update(@RequestBody Goodstype goodstype){
        return goodstypeService.updateById(goodstype)?Result.suc():Result.fail();
    }
    //删除
    @GetMapping("/del")
    public Result del(@RequestParam String id){
        return goodstypeService.removeById(id)?Result.suc():Result.fail();
    }
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query ){
        HashMap param=query.getParam();
        String name=(String) param.get("name");

        Page<Goodstype> page=new Page();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        LambdaQueryWrapper<Goodstype> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(Goodstype::getName,name);
        }

        IPage result=goodstypeService.page(page,lambdaQueryWrapper);

        return Result.suc(result.getRecords(),result.getTotal());
    }
    @GetMapping("/list")
    public Result list(){
        List list = goodstypeService.list();
        // 在内存中过滤包含 roleId 权限的菜单
        return Result.suc(list);
    }
}
