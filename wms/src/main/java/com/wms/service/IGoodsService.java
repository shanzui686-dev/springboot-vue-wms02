package com.wms.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.GoodsBatch;
import com.wms.entity.RestockSuggestion;

import java.util.List;
import java.util.Map;

public interface IGoodsService extends IService<Goods> {

    List<RestockSuggestion> suggestRestock(Integer purchaseDays);

    /** 查询某商品的批次库存详情（多仓库分布） */
    List<GoodsBatch> getBatchStock(Integer goodsId);

    /** FIFO 扣减库存：返回扣减后的批次记录列表，调用方据此更新 goods.count */
    void deductStockFIFO(Integer goodsId, Integer storageId, Integer quantity);

    /** 批次库存总览（跨商品分页查询） */
    IPage<Map<String, Object>> getBatchList(Integer pagenum, Integer pagesize,
                                            String goodsName, Integer supplierId, Integer storageId, String batchNo);
}


