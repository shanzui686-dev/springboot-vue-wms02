package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.GoodsVO;
import com.wms.mapper.GoodsBatchMapper;
import com.wms.mapper.GoodsMapper;
import com.wms.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;
    /**
     * 自动生成唯一批次号：yyyyMMddHHmm + 2位递增序号
     * 若传入的 batchNo 已是完整的14位（如20260530143001）则直接使用，否则按前缀自动生成
     */
    private String generateUniqueBatchNo(String batchNo) {
        if (batchNo == null || batchNo.isEmpty()) return batchNo;
        String prefix = batchNo.length() >= 12 ? batchNo.substring(0, 12) : batchNo;
        if (prefix.length() < 12) {
            prefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        }
        // 查当前 prefix 已有批次，取最大序号+1
        LambdaQueryWrapper<GoodsBatch> qw = new LambdaQueryWrapper<>();
        qw.likeRight(GoodsBatch::getBatchNo, prefix)
          .orderByDesc(GoodsBatch::getBatchNo)
          .last("LIMIT 1");
        List<GoodsBatch> list = goodsBatchMapper.selectList(qw);
        int seq = 1;
        if (!list.isEmpty()) {
            String lastNo = list.get(0).getBatchNo();
            try { seq = Integer.parseInt(lastNo.substring(12)) + 1; } catch (Exception e) { /* keep seq */ }
        }
        return prefix + String.format("%02d", seq);
    }

    //新增
    @Log("新增商品")
    @PostMapping("/save")
    public Result save(@RequestBody Goods goods){
        boolean saved = goodsService.save(goods);
        if (!saved) return Result.fail();

        // 如果填写了批次号且初始数量>0，自动创建初始批次
        if (goods.getBatchNo() != null && !goods.getBatchNo().isEmpty() && goods.getCount() != null && goods.getCount() > 0) {
            GoodsBatch batch = new GoodsBatch();
            batch.setGoodsId(goods.getId());
            batch.setBatchNo(generateUniqueBatchNo(goods.getBatchNo()));
            batch.setSupplierId(goods.getSupplierId());
            batch.setStorageId(goods.getStorage());
            batch.setPurchasePrice(goods.getPurchasePrice() != null ? goods.getPurchasePrice() : BigDecimal.ZERO);
            batch.setInitialCount(goods.getCount());
            batch.setCurrentCount(goods.getCount());
            goodsBatchMapper.insert(batch);
        }
        return Result.suc();
    }
    //更新
    @Log("更新商品")
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        Goods oldGoods = goodsService.getById(goods.getId());
        boolean updated = goodsService.updateById(goods);
        if (!updated) return Result.fail();

        // 编辑时如果填写了批次号，且数量有增加，则用增量创建新批次
        if (goods.getBatchNo() != null && !goods.getBatchNo().isEmpty()) {
            int oldCount = oldGoods != null ? (oldGoods.getCount() != null ? oldGoods.getCount() : 0) : 0;
            int newCount = goods.getCount() != null ? goods.getCount() : 0;
            int delta = newCount - oldCount;
            if (delta > 0) {
                GoodsBatch batch = new GoodsBatch();
                batch.setGoodsId(goods.getId());
                batch.setBatchNo(generateUniqueBatchNo(goods.getBatchNo()));
                batch.setSupplierId(goods.getSupplierId());
                batch.setStorageId(goods.getStorage());
                batch.setPurchasePrice(goods.getPurchasePrice() != null ? goods.getPurchasePrice() : BigDecimal.ZERO);
                batch.setInitialCount(delta);
                batch.setCurrentCount(delta);
                goodsBatchMapper.insert(batch);
            }
        }
        return Result.suc();
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
        Object supplierIdObj = param.get("supplierId");
        Object isWarningObj = param.get("isWarning");

        String categoryId = categoryIdObj != null ? categoryIdObj.toString() : null;
        String warehouseId = warehouseIdObj != null ? warehouseIdObj.toString() : null;
        String supplierId = supplierIdObj != null ? supplierIdObj.toString() : null;
        Boolean isWarning = isWarningObj != null ? Boolean.parseBoolean(isWarningObj.toString()) : false;
        
        Page<GoodsVO> page=new Page<>();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        
        // 使用连表查询，包含供应商名称、分类名称、仓库名称
        Integer categoryIdInt = StringUtils.isNotBlank(categoryId) && !"null".equals(categoryId) ? Integer.parseInt(categoryId) : null;
        Integer warehouseIdInt = StringUtils.isNotBlank(warehouseId) && !"null".equals(warehouseId) ? Integer.parseInt(warehouseId) : null;
        Integer supplierIdInt = StringUtils.isNotBlank(supplierId) && !"null".equals(supplierId) ? Integer.parseInt(supplierId) : null;

        IPage<GoodsVO> result=goodsMapper.selectGoodsWithSupplier(page, name, categoryIdInt, warehouseIdInt, supplierIdInt, isWarning);
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
     * 查询商品列表：传barcode按时条码精确查询，不传则返回全部商品（支持分页）
     * @param barcode 商品条码（可选）
     * @param page 页码（可选，默认1）
     * @param limit 每页数量（可选，默认1000）
     */
    @GetMapping("/list")
    public Result listByBarcode(@RequestParam(required = false) String barcode,
                                 @RequestParam(required = false, defaultValue = "1") Integer page,
                                 @RequestParam(required = false, defaultValue = "1000") Integer limit){
        // 按条码精确查询
        if(StringUtils.isNotBlank(barcode)){
            LambdaQueryWrapper<Goods> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Goods::getBarcode, barcode);
            List<Goods> goodsList = goodsService.list(lambdaQueryWrapper);
            if(goodsList.isEmpty()){
                return Result.fail("未找到该条码对应的商品");
            }
            return Result.suc(goodsList);
        }
        // 不传条码则返回全部商品（分页）
        Page<Goods> pageObj = new Page<>(page, limit);
        IPage<Goods> result = goodsService.page(pageObj);
        return Result.suc(result.getRecords(), result.getTotal());
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

    /** 查询某商品的批次库存详情（多仓库库存分布） */
    @GetMapping("/batchStock")
    public Result batchStock(@RequestParam Integer goodsId) {
        List<GoodsBatch> batches = goodsService.getBatchStock(goodsId);
        return Result.suc(batches);
    }

    /** 新增批次（批次号留空自动生成唯一序号） */
    @Log("新增批次")
    @PostMapping("/batch/save")
    public Result batchSave(@RequestBody GoodsBatch batch) {
        batch.setBatchNo(generateUniqueBatchNo(batch.getBatchNo()));
        goodsBatchMapper.insert(batch);
        return Result.suc();
    }

    /** 更新批次 */
    @Log("更新批次")
    @PostMapping("/batch/update")
    public Result batchUpdate(@RequestBody GoodsBatch batch) {
        goodsBatchMapper.updateById(batch);
        return Result.suc();
    }

    /** 删除批次 */
    @Log("删除批次")
    @GetMapping("/batch/del")
    public Result batchDel(@RequestParam Integer id) {
        goodsBatchMapper.deleteById(id);
        return Result.suc();
    }

    /** 批次库存总览（跨商品查询，支持按商品名/供应商/仓库/批次号筛选） */
    @PostMapping("/batchList")
    public Result batchList(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        String goodsName = (String) param.get("goodsName");
        Object supplierIdObj = param.get("supplierId");
        Object storageIdObj = param.get("storageId");
        String batchNo = (String) param.get("batchNo");
        Integer supplierId = supplierIdObj != null ? Integer.parseInt(supplierIdObj.toString()) : null;
        Integer storageId = storageIdObj != null ? Integer.parseInt(storageIdObj.toString()) : null;

        IPage<Map<String, Object>> result = goodsService.getBatchList(
            query.getPagenum(), query.getPagesize(), goodsName, supplierId, storageId, batchNo);
        return Result.suc(result.getRecords(), result.getTotal());
    }

    /** 查询指定仓库中有批次库存的商品（用于调拨选择商品） */
    @GetMapping("/listByStorage")
    public Result listByStorage(@RequestParam Integer storageId) {
        List<Map<String, Object>> goods = goodsMapper.selectGoodsByStorageId(storageId);
        return Result.suc(goods);
    }
}
