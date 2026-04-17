package com.wms.service;

import com.wms.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.RestockSuggestion;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wms
 * @since 2026-03-30
 */
public interface IGoodsService extends IService<Goods> {

    /**
     * 获取商品补货建议
     * @param purchaseDays 预计采购周期（天）
     * @return 补货建议列表
     */
    List<RestockSuggestion> suggestRestock(Integer purchaseDays);
}
