package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.RestockSuggestion;
import com.wms.mapper.GoodsBatchMapper;
import com.wms.mapper.GoodsMapper;
import com.wms.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;

    @Override
    public List<RestockSuggestion> suggestRestock(Integer purchaseDays) {
        if (purchaseDays == null || purchaseDays <= 0) {
            purchaseDays = 7;
        }

        List<Goods> allGoods = this.list();
        List<Map<String, Object>> salesData = this.baseMapper.selectRecentSales();

        Map<Integer, Integer> salesMap = new java.util.HashMap<>();
        for (Map<String, Object> row : salesData) {
            Object goodsIdObj = row.get("goodsId");
            Object totalSalesObj = row.get("totalSales");
            if (goodsIdObj != null && totalSalesObj != null) {
                Integer goodsId = ((Number) goodsIdObj).intValue();
                Integer totalSales = ((Number) totalSalesObj).intValue();
                salesMap.put(goodsId, totalSales);
            }
        }

        List<RestockSuggestion> suggestions = new ArrayList<>();
        for (Goods goods : allGoods) {
            RestockSuggestion suggestion = new RestockSuggestion();
            suggestion.setGoodsId(goods.getId());
            suggestion.setGoodsName(goods.getName());
            suggestion.setCurrentStock(goods.getCount());

            Integer recentSales = salesMap.getOrDefault(goods.getId(), 0);
            suggestion.setRecentSales(recentSales);

            BigDecimal avgDailySales = BigDecimal.valueOf(recentSales)
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            suggestion.setAvgDailySales(avgDailySales);

            // 可用库存 = 总库存 - 预占数量
            int reserved = goods.getReservedCount() != null ? goods.getReservedCount() : 0;
            int availableStock = goods.getCount() - reserved;

            BigDecimal expectedDemand = avgDailySales.multiply(BigDecimal.valueOf(purchaseDays));
            int suggestQuantity = expectedDemand.subtract(BigDecimal.valueOf(availableStock)).intValue();
            if (suggestQuantity < 0) {
                suggestQuantity = 0;
            }
            suggestion.setSuggestQuantity(suggestQuantity);

            suggestions.add(suggestion);
        }
        return suggestions;
    }

    @Override
    public List<GoodsBatch> getBatchStock(Integer goodsId) {
        return this.baseMapper.selectBatchesByGoodsId(goodsId);
    }

    /**
     * FIFO 扣减库存（事务保护）
     * 1. 按 create_time ASC 查出可用批次
     * 2. 逐批扣减 current_count，直到满足 quantity
     * 3. 同步更新 goods.count
     * 4. 如果库存不足，抛出异常回滚
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStockFIFO(Integer goodsId, Integer storageId, Integer quantity) {
        List<GoodsBatch> batches = this.baseMapper
                .selectAvailableBatchesForSale(goodsId, storageId);

        int remaining = quantity;
        for (GoodsBatch batch : batches) {
            if (remaining <= 0) break;
            int deduct = Math.min(batch.getCurrentCount(), remaining);
            batch.setCurrentCount(batch.getCurrentCount() - deduct);
            goodsBatchMapper.updateById(batch);
            remaining -= deduct;
        }

        if (remaining > 0) {
            throw new RuntimeException("库存不足：商品ID=" + goodsId + ", 需要=" + quantity + ", 不足=" + remaining);
        }

        // 同步 goods.count
        Goods goods = this.getById(goodsId);
        goods.setCount(goods.getCount() - quantity);
        this.updateById(goods);
    }

    @Override
    public IPage<Map<String, Object>> getBatchList(Integer pagenum, Integer pagesize,
                                                    String goodsName, Integer supplierId, Integer storageId, String batchNo) {
        Page<Map<String, Object>> page = new Page<>(pagenum, pagesize);
        return this.baseMapper.selectBatchList(page, goodsName, supplierId, storageId, batchNo);
    }
}
