package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Goods;
import com.wms.entity.Sales;
import com.wms.entity.SalesDTO;
import com.wms.entity.SalesDetail;
import com.wms.entity.SalesDetailVO;
import com.wms.entity.SalesReturn;
import com.wms.entity.SalesVO;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.SalesDetailMapper;
import com.wms.mapper.SalesMapper;
import com.wms.mapper.SalesReturnMapper;
import com.wms.service.IGoodsService;
import com.wms.service.ISalesDetailService;
import com.wms.service.ISalesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 销售单服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-04-15
 */
@Service
public class SalesServiceImpl extends ServiceImpl<SalesMapper, Sales> implements ISalesService {

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private ISalesDetailService salesDetailService;

    @Autowired
    private SalesMapper salesMapper;

    @Autowired
    private SalesDetailMapper salesDetailMapper;

    @Autowired
    private SalesReturnMapper salesReturnMapper;

    /**
     * 收银结算（事务处理）
     * @param salesDTO 销售单数据传输对象
     * @return 订单ID（流水号）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer checkout(SalesDTO salesDTO) {
        // 1. 获取销售明细列表
        List<SalesDetail> details = salesDTO.getDetails();
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("销售明细不能为空");
        }

        // 2. 遍历明细，检查库存并扣减
        for (SalesDetail detail : details) {
            // 根据 goodsId 查询商品信息
            Goods goods = goodsService.getById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + detail.getGoodsId() + "】不存在");
            }

            // 判断库存是否充足
            if (goods.getCount() < detail.getCount()) {
                throw new RuntimeException("商品【" + goods.getName() + "】库存不足，当前库存：" + goods.getCount() + "，需求数量：" + detail.getCount());
            }

            // 扣减库存
            goods.setCount(goods.getCount() - detail.getCount());
            boolean updateResult = goodsService.updateById(goods);
            if (!updateResult) {
                throw new RuntimeException("商品【" + goods.getName() + "】库存扣减失败");
            }
        }

        // 3. 插入销售主表数据
        Sales sales = new Sales();
        sales.setUserId(salesDTO.getUserId());
        sales.setTotalAmount(salesDTO.getTotalAmount());
        sales.setRealAmount(salesDTO.getRealAmount());
        sales.setChangeAmount(salesDTO.getChangeAmount());
        sales.setCreateTime(LocalDateTime.now());

        boolean saveResult = this.save(sales);
        if (!saveResult) {
            throw new RuntimeException("销售单保存失败");
        }

        // 4. 获取自动生成的主键 ID（订单流水号）
        Integer salesId = sales.getId();

        // 5. 将主键 ID 赋值给每个明细，并批量插入销售明细表
        for (SalesDetail detail : details) {
            detail.setSalesId(salesId);
        }

        boolean saveBatchResult = salesDetailService.saveBatch(details);
        if (!saveBatchResult) {
            throw new RuntimeException("销售明细保存失败");
        }

        // 6. 返回订单流水号
        return salesId;
    }

    /**
     * 分页查询销售单列表（连表查询收银员姓名）
     * @param page 分页对象
     * @param orderNum 订单流水号（模糊匹配）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @Override
    public IPage<SalesVO> listPage(Page<SalesVO> page, String orderNum, String startDate, String endDate) {
        // 调用 Mapper 的自定义查询方法，实现连表查询和条件过滤
        return salesMapper.selectSalesPage(page, orderNum, startDate, endDate);
    }

    /**
     * 查询销售明细列表（连表查询商品名称）
     * @param salesId 销售单ID
     * @return 销售明细列表
     */
    @Override
    public List<SalesDetailVO> getDetails(Integer salesId) {
        // 调用 Mapper 的自定义查询方法，连表查询商品名称
        return salesDetailMapper.selectDetailsBySalesId(salesId);
    }

    /**
     * 收银台直接退款（回滚库存）
     * @param orderNum 销售单流水号
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refund(String orderNum) {
        // 1. 根据流水号查询销售单
        LambdaQueryWrapper<Sales> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sales::getOrderNum, orderNum);
        Sales sales = this.getOne(queryWrapper);
        
        if (sales == null) {
            throw new RuntimeException("销售单【" + orderNum + "】不存在");
        }

        Integer salesId = sales.getId();

        // 2. 检查是否已经退款（查询是否有已退款的退货记录）
        LambdaQueryWrapper<SalesReturn> returnQuery = new LambdaQueryWrapper<>();
        returnQuery.eq(SalesReturn::getSalesId, salesId)
                   .eq(SalesReturn::getStatus, 1); // 已退款
        Long refundedCount = salesReturnMapper.selectCount(returnQuery);
        if (refundedCount > 0) {
            throw new RuntimeException("该销售单已退款，无需重复操作");
        }

        // 3. 查询销售明细
        LambdaQueryWrapper<SalesDetail> detailQuery = new LambdaQueryWrapper<>();
        detailQuery.eq(SalesDetail::getSalesId, salesId);
        List<SalesDetail> details = salesDetailService.list(detailQuery);

        if (details == null || details.isEmpty()) {
            throw new RuntimeException("销售明细不存在");
        }

        // 4. 遍历明细，回滚库存
        for (SalesDetail detail : details) {
            Goods goods = goodsService.getById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID【" + detail.getGoodsId() + "】不存在");
            }

            // 增加库存
            Integer currentCount = goods.getCount() == null ? 0 : goods.getCount();
            goods.setCount(currentCount + detail.getCount());

            boolean updateResult = goodsService.updateById(goods);
            if (!updateResult) {
                throw new RuntimeException("商品【" + goods.getName() + "】库存回滚失败");
            }
        }

        // 5. 创建退货记录（状态设为已退款）
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setReturnNo(sales.getOrderNum());
        salesReturn.setReturnNum("REF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        salesReturn.setSalesId(salesId);
        salesReturn.setReturnReason("收银台直接退款");
        salesReturn.setReturnAmount(sales.getRealAmount());
        salesReturn.setStatus(1); // 已退款
        salesReturn.setCreateTime(LocalDateTime.now());
        salesReturn.setRefundTime(LocalDateTime.now());
        salesReturn.setUserId(sales.getUserId());

        boolean saveReturnResult = salesReturnMapper.insert(salesReturn) > 0;
        if (!saveReturnResult) {
            throw new RuntimeException("退货记录保存失败");
        }

        return true;
    }
}
