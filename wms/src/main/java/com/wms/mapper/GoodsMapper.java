package com.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.GoodsVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    @MapKey("goodsId")
    List<Map<String, Object>> selectRecentSales();

    IPage<GoodsVO> selectGoodsWithSupplier(Page<GoodsVO> page, @Param("name") String name, @Param("goodsType") Integer goodsType, @Param("storage") Integer storage, @Param("showUrgentOnly") Boolean showUrgentOnly);

}
