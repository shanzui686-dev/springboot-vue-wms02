package com.wms.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Purchase;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.PurchaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 采购主表 Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2026-04-20
 */
@Mapper
public interface PurchaseMapper extends BaseMapper<Purchase> {

    /**
     * 分页查询采购单（连表查询供应商和采购员信息）
     * @param page 分页对象
     * @param purchaseNo 采购单号
     * @param supplierId 供应商ID
     * @param status 状态
     * @return 分页结果
     */
    IPage<PurchaseVO> selectPurchaseWithDetails(Page<PurchaseVO> page, 
                                                 @Param("purchaseNo") String purchaseNo,
                                                 @Param("supplierId") Integer supplierId,
                                                 @Param("status") Integer status);

}
