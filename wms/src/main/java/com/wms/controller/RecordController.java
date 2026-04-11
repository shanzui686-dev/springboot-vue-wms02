package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.mapper.RecordMapper;
import com.wms.service.IGoodsService;
import com.wms.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/record")
public class RecordController {
    @Autowired
    private IRecordService recordService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RecordMapper recordMapper;
    @PostMapping("/listPageCC")
    public Result listPageCC(@RequestBody QueryPageParam query ){
        HashMap param=query.getParam();
        String name=(String) param.get("name");
        Object goodstypeObj = param.get("goodstype");
        Object storageObj = param.get("storage");
        Object roleIdObj = param.get("roleId");
        Object userIdObj = param.get("userId");
        String goodstype = goodstypeObj != null ? goodstypeObj.toString() : null;
        String storage = storageObj != null ? storageObj.toString() : null;
        String roleId = roleIdObj != null ? roleIdObj.toString() : null;
        String userId = userIdObj != null ? userIdObj.toString() : null;
        
        Page<Record> page=new Page();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        
        QueryWrapper<Record> queryWrapper=new QueryWrapper<>();
        queryWrapper.apply(" a.goods=b.id and b.storage=c.id and b.goodsType=d.id ");
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            queryWrapper.like("b.name", name);
        }
        if(StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)){
            queryWrapper.eq("d.id",Integer.parseInt(goodstype));
        }
        if(StringUtils.isNotBlank(storage) && !"null".equals(storage)){
            queryWrapper.eq("c.id",Integer.parseInt(storage));
        }
        if("2".equals(roleId)){
            queryWrapper.eq("a.userId", userId);
        }

        
        // 使用自定义查询
        IPage result=recordMapper.pageCC(page,queryWrapper);
        return Result.suc(result.getRecords(),result.getTotal());
    }
    //新增
    @PostMapping("/save")
    public Result save(@RequestBody Record record){
        System.out.println("========== 入库记录 ==========");
        System.out.println("完整记录: " + record);
        System.out.println("商品ID: " + record.getGoods());
        System.out.println("申请人ID (userId): " + record.getUserId());
        System.out.println("操作人ID (adminId): " + record.getAdminId());
        System.out.println("数量: " + record.getCount());
        System.out.println("备注: " + record.getRemark());
        
        if(record.getGoods() == null){
            return Result.fail("商品ID不能为空");
        }
        
        Goods goods = goodsService.getById(record.getGoods());
        if(goods == null){
            return Result.fail("商品不存在");
        }
        
        System.out.println("原数量: " + goods.getCount());
        System.out.println("入库数量: " + record.getCount());
        
        int num = goods.getCount() + record.getCount();
        goods.setCount(num);
        
        boolean updated = goodsService.updateById(goods);
        System.out.println("更新结果: " + updated);
        System.out.println("新数量: " + goods.getCount());
        
        // 设置操作时间为当前时间
        record.setCreatetime(LocalDateTime.now());
        
        return recordService.save(record)?Result.suc():Result.fail();
    }
    
    // 出库
    @PostMapping("/out")
    public Result out(@RequestBody Record record){
        System.out.println("========== 出库记录 ==========");
        System.out.println("完整记录: " + record);
        System.out.println("商品ID: " + record.getGoods());
        System.out.println("申请人ID (userId): " + record.getUserId());
        System.out.println("操作人ID (adminId): " + record.getAdminId());
        System.out.println("数量: " + record.getCount());
        System.out.println("备注: " + record.getRemark());
        
        if(record.getGoods() == null){
            return Result.fail("商品ID不能为空");
        }
        
        if(record.getCount() == null || record.getCount() <= 0){
            return Result.fail("出库数量必须大于0");
        }
        
        Goods goods = goodsService.getById(record.getGoods());
        if(goods == null){
            return Result.fail("商品不存在");
        }
        
        System.out.println("原数量: " + goods.getCount());
        System.out.println("出库数量: " + record.getCount());
        
        if(goods.getCount() < record.getCount()){
            return Result.fail("库存不足！当前库存：" + goods.getCount());
        }
        
        int num = goods.getCount() - record.getCount();
        goods.setCount(num);
        
        boolean updated = goodsService.updateById(goods);
        System.out.println("更新结果: " + updated);
        System.out.println("新数量: " + goods.getCount());
        
        // 设置操作时间为当前时间，出库数量存为负数
        record.setCount(-record.getCount());
        record.setCreatetime(LocalDateTime.now());
        
        return recordService.save(record)?Result.suc():Result.fail();
    }
}
