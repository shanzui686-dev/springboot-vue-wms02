package com.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.GoodsBatch;
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

    IPage<GoodsVO> selectGoodsWithSupplier(Page<GoodsVO> page,
                                           @Param("name") String name,
                                           @Param("goodsType") Integer goodsType,
                                           @Param("storage") Integer storage,
                                           @Param("supplierId") Integer supplierId,
                                           @Param("showUrgentOnly") Boolean showUrgentOnly);

    /** 查询某商品在所有仓库的批次库存（多仓库库存展示） */
    List<GoodsBatch> selectBatchesByGoodsId(@Param("goodsId") Integer goodsId);

    /** 批次库存总览（跨商品查询，支持按商品/供应商/批次筛选） */
    IPage<Map<String, Object>> selectBatchList(Page<?> page,
                                               @Param("goodsName") String goodsName,
                                               @Param("supplierId") Integer supplierId,
                                               @Param("storageId") Integer storageId,
                                               @Param("batchNo") String batchNo);

    /** FIFO：按入库时间升序，查询某商品在指定仓库中 currentCount>0 的批次 */
    List<GoodsBatch> selectAvailableBatchesForSale(
            @Param("goodsId") Integer goodsId,
            @Param("storageId") Integer storageId
    );

    /** 查询指定仓库中有批次库存的商品列表（用于调拨选择商品） */
    List<Map<String, Object>> selectGoodsByStorageId(@Param("storageId") Integer storageId);
}


