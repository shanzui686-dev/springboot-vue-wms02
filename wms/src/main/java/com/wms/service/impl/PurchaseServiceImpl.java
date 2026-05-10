package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Goods;
import com.wms.entity.Purchase;
import com.wms.entity.PurchaseDetail;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.PurchaseDetailMapper;
import com.wms.mapper.PurchaseMapper;
import com.wms.entity.PurchaseDTO;
import com.wms.entity.PurchaseVO;
import com.wms.entity.Record;
import com.wms.service.IRecordService;
import com.wms.service.IPurchaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 采购主表 服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-04-20
 */
@Service
public class PurchaseServiceImpl extends ServiceImpl<PurchaseMapper, Purchase> implements IPurchaseService {

    @Autowired
    private PurchaseMapper purchaseMapper;

    @Autowired
    private PurchaseDetailMapper purchaseDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private IRecordService recordService;

    /**
     * 分页查询采购单（连表查询）
     */
    @Override
    public IPage<PurchaseVO> listPage(Integer pagenum, Integer pagesize, String purchaseNo, Integer supplierId, Integer status) {
        Page<PurchaseVO> page = new Page<>();
        page.setCurrent(pagenum);
        page.setSize(pagesize);

        return purchaseMapper.selectPurchaseWithDetails(page, purchaseNo, supplierId, status);
    }

    /**
     * 保存采购单（包含明细）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithDetails(PurchaseDTO purchaseDTO) {
        // 1. 保存采购主表
        Purchase purchase = new Purchase();
        BeanUtils.copyProperties(purchaseDTO, purchase);
        boolean saveResult = this.save(purchase);

        if (!saveResult) {
            return false;
        }

        // 2. 保存采购明细
        List<PurchaseDTO.PurchaseDetailItem> details = purchaseDTO.getDetails();
        if (details != null && !details.isEmpty()) {
            List<PurchaseDetail> detailList = new ArrayList<>();
            for (PurchaseDTO.PurchaseDetailItem item : details) {
                PurchaseDetail detail = new PurchaseDetail();
                BeanUtils.copyProperties(item, detail);
                detail.setPurchaseId(purchase.getId());
                detailList.add(detail);
            }

            // 批量插入明细
            for (PurchaseDetail detail : detailList) {
                purchaseDetailMapper.insert(detail);
            }
        }

        return true;
    }

    /**
     * 更新采购单（包含明细）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateWithDetails(PurchaseDTO purchaseDTO) {
        // 1. 更新采购主表
        Purchase purchase = new Purchase();
        BeanUtils.copyProperties(purchaseDTO, purchase);
        boolean updateResult = this.updateById(purchase);

        if (!updateResult) {
            return false;
        }

        // 2. 删除原有明细
        LambdaQueryWrapper<PurchaseDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetail::getPurchaseId, purchaseDTO.getId());
        purchaseDetailMapper.delete(queryWrapper);

        // 3. 保存新明细
        List<PurchaseDTO.PurchaseDetailItem> details = purchaseDTO.getDetails();
        if (details != null && !details.isEmpty()) {
            List<PurchaseDetail> detailList = new ArrayList<>();
            for (PurchaseDTO.PurchaseDetailItem item : details) {
                PurchaseDetail detail = new PurchaseDetail();
                BeanUtils.copyProperties(item, detail);
                detail.setPurchaseId(purchaseDTO.getId());
                detailList.add(detail);
            }

            // 批量插入明细
            for (PurchaseDetail detail : detailList) {
                purchaseDetailMapper.insert(detail);
            }
        }

        return true;
    }

    /**
     * 创建采购单（计算总金额，默认状态为待入库）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPurchase(PurchaseDTO purchaseDTO) {
        // 1. 验证明细列表
        List<PurchaseDTO.PurchaseDetailItem> details = purchaseDTO.getDetails();
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("采购明细不能为空");
        }

        // 2. 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseDTO.PurchaseDetailItem item : details) {
            if (item.getCount() == null || item.getCount() <= 0) {
                throw new RuntimeException("商品数量必须大于0");
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("商品价格不能为负数");
            }
            // 计算小计：数量 * 单价
            BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getCount()));
            item.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);
        }

        // 3. 设置采购单信息
        Purchase purchase = new Purchase();
        BeanUtils.copyProperties(purchaseDTO, purchase);
        
        // 修复：确保采购员ID有效（前端已传入当前登录用户ID，此处仅作防御性检查）
        if (purchase.getUserId() == null || purchase.getUserId() == 0) {
            throw new RuntimeException("采购员ID不能为空，请重新登录后再试");
        }
        
        purchase.setTotalAmount(totalAmount);
        purchase.setStatus(0); // 默认状态：待入库

        // 4. 保存采购主表
        boolean saveResult = this.save(purchase);
        if (!saveResult) {
            throw new RuntimeException("采购单创建失败");
        }

        // 5. 保存采购明细
        List<PurchaseDetail> detailList = new ArrayList<>();
        for (PurchaseDTO.PurchaseDetailItem item : details) {
            PurchaseDetail detail = new PurchaseDetail();
            BeanUtils.copyProperties(item, detail);
            detail.setPurchaseId(purchase.getId());
            detailList.add(detail);
        }

        // 批量插入明细
        for (PurchaseDetail detail : detailList) {
            purchaseDetailMapper.insert(detail);
        }

        return true;
    }

    /**
     * 确认入库（更新状态为已入库，增加商品库存）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inbound(Integer purchaseId) {
        // 1. 查询采购单
        Purchase purchase = this.getById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }

        // 2. 检查状态
        if (purchase.getStatus() != 0) {
            throw new RuntimeException("只有待入库状态的采购单才能确认入库");
        }

        // 3. 查询采购明细
        LambdaQueryWrapper<PurchaseDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetail::getPurchaseId, purchaseId);
        List<PurchaseDetail> details = purchaseDetailMapper.selectList(queryWrapper);

        if (details == null || details.isEmpty()) {
            throw new RuntimeException("采购明细不存在");
        }

        // 4. 遍历明细，更新商品库存
        for (PurchaseDetail detail : details) {
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID " + detail.getGoodsId() + " 不存在");
            }

            // 增加库存
            Integer currentCount = goods.getCount() != null ? goods.getCount() : 0;
            goods.setCount(currentCount + detail.getCount());

            // 如果采购价有变动，同步更新商品进价
            if (detail.getPrice() != null) {
                goods.setPurchasePrice(detail.getPrice());
            }

            // 更新商品
            int updateResult = goodsMapper.updateById(goods);
            if (updateResult <= 0) {
                throw new RuntimeException("商品库存更新失败");
            }

            // 4.1 记录出入库流水
            Record record = new Record();
            record.setGoods(detail.getGoodsId());
            record.setCount(detail.getCount()); // 数量为正
            record.setOperationType("采购入库");
            record.setRefOrderNum(purchase.getPurchaseNo() != null ? purchase.getPurchaseNo() : purchase.getId().toString());
            record.setAdminId(purchase.getUserId());
            record.setCreatetime(java.time.LocalDateTime.now());
            record.setStatus(1); // 已完成
            recordService.save(record);
        }

        // 5. 更新采购单状态为已入库
        purchase.setStatus(1);
        boolean updateResult = this.updateById(purchase);
        if (!updateResult) {
            throw new RuntimeException("采购单状态更新失败");
        }

        return true;
    }

    /**
     * 采购退货（更新状态为已退货，扣减商品库存）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnGoods(Integer purchaseId) {
        // 1. 查询采购单
        Purchase purchase = this.getById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }

        // 2. 检查状态（必须是已入库状态才能退货）
        if (purchase.getStatus() != 1) {
            throw new RuntimeException("只有已入库状态的采购单才能退货");
        }

        // 3. 查询采购明细
        LambdaQueryWrapper<PurchaseDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetail::getPurchaseId, purchaseId);
        List<PurchaseDetail> details = purchaseDetailMapper.selectList(queryWrapper);

        if (details == null || details.isEmpty()) {
            throw new RuntimeException("采购明细不存在");
        }

        // 4. 先检查所有商品的库存是否足够扣减
        for (PurchaseDetail detail : details) {
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID " + detail.getGoodsId() + " 不存在");
            }

            Integer currentCount = goods.getCount() != null ? goods.getCount() : 0;
            if (currentCount < detail.getCount()) {
                throw new RuntimeException("商品【" + goods.getName() + "】库存不足，当前库存：" + currentCount + "，需要扣减：" + detail.getCount());
            }
        }

        // 5. 遍历明细，扣减商品库存
        for (PurchaseDetail detail : details) {
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            
            // 扣减库存
            Integer currentCount = goods.getCount() != null ? goods.getCount() : 0;
            goods.setCount(currentCount - detail.getCount());

            // 更新商品
            int updateResult = goodsMapper.updateById(goods);
            if (updateResult <= 0) {
                throw new RuntimeException("商品库存扣减失败");
            }

            // 5.1 记录出入库流水
            Record record = new Record();
            record.setGoods(detail.getGoodsId());
            record.setCount(-detail.getCount()); // 数量为负
            record.setOperationType("采购退货");
            record.setRefOrderNum(purchase.getPurchaseNo() != null ? purchase.getPurchaseNo() : purchase.getId().toString());
            record.setAdminId(purchase.getUserId());
            record.setCreatetime(java.time.LocalDateTime.now());
            record.setStatus(1); // 已完成
            recordService.save(record);
        }

        // 6. 更新采购单状态为已退货
        purchase.setStatus(2);
        boolean updateResult = this.updateById(purchase);
        if (!updateResult) {
            throw new RuntimeException("采购单状态更新失败");
        }

        return true;
    }
}
