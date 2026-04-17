package com.wms.service.impl;

import com.wms.entity.Goods;
import com.wms.mapper.GoodsMapper;
import com.wms.service.IGoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.RestockSuggestion;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-03-30
 */
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {

    /**
     * 获取商品补货建议
     * 算法：建议采购量 = (近30天日均销量 * 预计采购周期) - 当前库存
     * 
     * @param purchaseDays 预计采购周期（天）
     * @return 补货建议列表
     */
    @Override
    public List<RestockSuggestion> suggestRestock(Integer purchaseDays) {
        // 参数校验
        if (purchaseDays == null || purchaseDays <= 0) {
            purchaseDays = 7; // 默认7天采购周期
        }

        // 1. 查询所有商品
        List<Goods> allGoods = this.list();

        // 2. 查询近30天各商品的销售量
        List<Map<String, Object>> salesData = this.baseMapper.selectRecentSales();

        // 3. 将销售数据转换为 Map 方便查找：goodsId -> totalSales
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

        // 4. 遍历所有商品，计算补货建议
        List<RestockSuggestion> suggestions = new ArrayList<>();
        for (Goods goods : allGoods) {
            RestockSuggestion suggestion = new RestockSuggestion();
            suggestion.setGoodsId(goods.getId());
            suggestion.setGoodsName(goods.getName());
            suggestion.setCurrentStock(goods.getCount());

            // 获取近30天销量（如果该商品没有销售记录，则为0）
            Integer recentSales = salesMap.getOrDefault(goods.getId(), 0);
            suggestion.setRecentSales(recentSales);

            // 计算日均销量：总销量 / 30
            BigDecimal avgDailySales = BigDecimal.valueOf(recentSales)
                    .divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP);
            suggestion.setAvgDailySales(avgDailySales);

            // 计算建议采购量：(日均销量 * 采购周期) - 当前库存
            // 日均销量 * 采购周期 = 预计需求总量
            BigDecimal expectedDemand = avgDailySales.multiply(BigDecimal.valueOf(purchaseDays));
            int suggestQuantity = expectedDemand.subtract(BigDecimal.valueOf(goods.getCount()))
                    .intValue();

            // 如果建议量小于0，则设为0（不需要补货）
            if (suggestQuantity < 0) {
                suggestQuantity = 0;
            }
            suggestion.setSuggestQuantity(suggestQuantity);

            suggestions.add(suggestion);
        }

        return suggestions;
    }
}
