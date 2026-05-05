package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.*;
import com.wms.mapper.SalesReturnDetailMapper;
import com.wms.mapper.SalesReturnMapper;
import com.wms.service.IGoodsService;
import com.wms.service.ISalesReturnDetailService;
import com.wms.service.ISalesReturnService;
import com.wms.service.ISalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 退货单服务实现类
 */
@Service
public class SalesReturnServiceImpl extends ServiceImpl<SalesReturnMapper, SalesReturn> implements ISalesReturnService {

    @Autowired
    private ISalesService salesService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISalesReturnDetailService salesReturnDetailService;

    @Autowired
    private SalesReturnMapper salesReturnMapper;

    @Autowired
    private SalesReturnDetailMapper salesReturnDetailMapper;

    /**
     * 发起退货申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyReturn(ReturnApplyDTO returnApplyDTO) {
        // 1. 参数校验
        if (returnApplyDTO.getSalesId() == null) {
            throw new RuntimeException("原销售单ID不能为空");
        }
        if (returnApplyDTO.getItems() == null || returnApplyDTO.getItems().isEmpty()) {
            throw new RuntimeException("退货商品明细不能为空");
        }

        // 2. 校验原销售单是否存在
        Sales sales = salesService.getById(returnApplyDTO.getSalesId());
        if (sales == null) {
            throw new RuntimeException("原销售单不存在");
        }

        // 3. 遍历校验退货数量是否合法
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReturnApplyDTO.ReturnItemDTO item : returnApplyDTO.getItems()) {
            // 校验商品是否存在
            Goods goods = goodsService.getById(item.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + item.getGoodsId() + "】不存在");
            }

            // 校验退货数量不能为负数
            if (item.getReturnCount() == null || item.getReturnCount() <= 0) {
                throw new RuntimeException("商品【" + goods.getName() + "】退货数量必须大于0");
            }

            // 计算小计金额
            BigDecimal subtotal = goods.getRetailPrice()
                    .multiply(BigDecimal.valueOf(item.getReturnCount()));
            totalAmount = totalAmount.add(subtotal);
        }

        // 4. 生成退货单流水号（RET + 年月日时分秒）
        String returnNum = generateReturnNo();

        // 5. 插入退货主表（状态默认为0待退款）
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnNo(sales.getOrderNum()); // 关联原销售单号
        salesReturn.setReturnNum(returnNum); // 退货单流水号
        salesReturn.setSalesId(returnApplyDTO.getSalesId());
        salesReturn.setReturnReason(returnApplyDTO.getReturnReason());
        salesReturn.setReturnAmount(totalAmount);
        salesReturn.setStatus(0); // 0待退款
        salesReturn.setCreateTime(LocalDateTime.now());
        salesReturn.setUserId(null); // 可从当前登录用户获取

        boolean saveResult = this.save(salesReturn);
        if (!saveResult) {
            throw new RuntimeException("退货单保存失败");
        }

        // 6. 获取自动生成的主键ID
        Integer returnId = salesReturn.getId();

        // 7. 批量插入退货明细表
        List<SalesReturnDetail> detailList = new java.util.ArrayList<>();
        for (ReturnApplyDTO.ReturnItemDTO item : returnApplyDTO.getItems()) {
            Goods goods = goodsService.getById(item.getGoodsId());
            
            SalesReturnDetail detail = new SalesReturnDetail();
            detail.setReturnId(returnId);
            detail.setGoodsId(item.getGoodsId());
            detail.setReturnCount(item.getReturnCount());
            detail.setPrice(goods.getRetailPrice());
            detail.setSubtotal(goods.getRetailPrice().multiply(BigDecimal.valueOf(item.getReturnCount())));
            
            detailList.add(detail);
        }

        boolean saveBatchResult = salesReturnDetailService.saveBatch(detailList);
        if (!saveBatchResult) {
            throw new RuntimeException("退货明细保存失败");
        }

        // 8. 返回退货单ID
        return returnId;
    }

    /**
     * 确认退款并回滚库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmRefund(Integer returnId) {
        // 1. 校验退货单是否存在
        SalesReturn salesReturn = this.getById(returnId);
        if (salesReturn == null) {
            throw new RuntimeException("退货单不存在");
        }

        // 2. 校验退货单状态
        if (salesReturn.getStatus() == 1) {
            throw new RuntimeException("该退货单已退款，无需重复操作");
        }

        // 3. 查询退货明细
        LambdaQueryWrapper<SalesReturnDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SalesReturnDetail::getReturnId, returnId);
        List<SalesReturnDetail> detailList = salesReturnDetailService.list(queryWrapper);

        if (detailList == null || detailList.isEmpty()) {
            throw new RuntimeException("退货明细不存在");
        }

        // 4. 遍历明细，回滚库存
        for (SalesReturnDetail detail : detailList) {
            // 查询商品信息
            Goods goods = goodsService.getById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + detail.getGoodsId() + "】不存在");
            }

            // 增加库存
            Integer currentCount = goods.getCount() == null ? 0 : goods.getCount();
            goods.setCount(currentCount + detail.getReturnCount());
            
            boolean updateResult = goodsService.updateById(goods);
            if (!updateResult) {
                throw new RuntimeException("商品【" + goods.getName() + "】库存回滚失败");
            }
        }

        // 5. 更新退货单状态为已退款
        salesReturn.setStatus(1);
        salesReturn.setRefundTime(LocalDateTime.now());
        boolean updateResult = this.updateById(salesReturn);
        
        return updateResult;
    }

    /**
     * 分页查询退货单列表
     */
    @Override
    public IPage<SalesReturnVO> listPage(Page<SalesReturnVO> page, String returnNo, Integer status) {
        return salesReturnMapper.selectReturnPage(page, returnNo, status);
    }

    /**
     * 查询退货明细列表
     */
    @Override
    public List<ReturnDetailVO> getDetails(Integer returnId) {
        return salesReturnDetailMapper.selectDetailsByReturnId(returnId);
    }

    /**
     * 生成退货单号
     * 格式：RET + 年月日时分秒（如：RET20260501153025）
     */
    private String generateReturnNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "RET" + timestamp;
    }
}
