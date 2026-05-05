package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Supplier;
import com.wms.service.ISupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 供应商前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-04-17
 */
@RestController
@RequestMapping("/supplier")
public class SupplierController {
    private static final Logger log = LoggerFactory.getLogger(SupplierController.class);
    
    @Autowired
    private ISupplierService supplierService;

    // 新增
    @Log("新增供应商")
    @PostMapping("/save")
    public Result save(@RequestBody Supplier supplier){
        return supplierService.save(supplier) ? Result.suc() : Result.fail();
    }

    // 更新
    @Log("更新供应商")
    @PostMapping("/update")
    public Result update(@RequestBody Supplier supplier){
        return supplierService.updateById(supplier) ? Result.suc() : Result.fail();
    }

    // 删除
    @Log("删除供应商")
    @GetMapping("/del")
    public Result del(@RequestParam String id){
        return supplierService.removeById(id) ? Result.suc() : Result.fail();
    }

    // 分页查询
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String) param.get("name");
        String contact = (String) param.get("contact");

        Page<Supplier> page = new Page<>();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());

        LambdaQueryWrapper<Supplier> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(Supplier::getName, name);
        }
        if(StringUtils.isNotBlank(contact) && !"null".equals(contact)){
            lambdaQueryWrapper.like(Supplier::getContact, contact);
        }

        IPage result = supplierService.page(page, lambdaQueryWrapper);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    // 查询所有
    @GetMapping("/list")
    public Result list(){
        List<Supplier> list = supplierService.list();
        return Result.suc(list);
    }

    /**
     * 更新供应商状态（启用/禁用）
     * @param id 供应商ID
     * @param status 状态：1启用，0禁用
     * @return 操作结果
     */
    @Log("更新供应商状态")
    @PostMapping("/updateStatus")
    public Result updateStatus(@RequestParam Integer id, @RequestParam Integer status){
        if(id == null || id <= 0){
            return Result.fail().msg("供应商ID不能为空");
        }
        if(status == null || (status != 0 && status != 1)){
            return Result.fail().msg("状态参数错误，必须为0或1");
        }

        Supplier supplier = supplierService.getById(id);
        if(supplier == null){
            return Result.fail().msg("供应商不存在");
        }

        // 如果是禁用操作，记录日志并提醒
        if(status == 0){
            log.warn("供应商 [{}] 被禁用，禁用后该供应商关联的商品将无法进行采购入库操作", supplier.getName());
            supplierService.updateStatus(id, status);
            return Result.suc().msg("已禁用供应商：" + supplier.getName() + "。注意：禁用后，该供应商关联的商品将无法进行采购入库操作");
        } else {
            supplierService.updateStatus(id, status);
            return Result.suc().msg("已启用供应商：" + supplier.getName());
        }
    }
}
