package com.wms.mapper;

import com.wms.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-03-30
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    /**
     * 查询近30天各商品的销售量
     * @return 商品ID和销售总量的列表
     */
    List<Map<String, Object>> selectRecentSales();

}
