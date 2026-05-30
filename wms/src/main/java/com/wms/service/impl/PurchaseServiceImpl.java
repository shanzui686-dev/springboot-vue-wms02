package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.entity.Goods;
import com.wms.entity.GoodsBatch;
import com.wms.entity.InboundDTO;
import com.wms.entity.Purchase;
import com.wms.entity.PurchaseDetail;
import com.wms.mapper.GoodsBatchMapper;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;

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
     * 创建采购单（计算总金额，自动生成批次号，默认状态为待审核）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createPurchase(PurchaseDTO purchaseDTO) {
        // 1. 验证明细列表
        List<PurchaseDTO.PurchaseDetailItem> details = purchaseDTO.getDetails();
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("采购明细不能为空");
        }

        // 生成统一的批次号前缀：yyyyMMddHHmm
        String batchPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));

        // 查询当前分钟内已有批次的最大序号，避免跨采购单批次号重复
        LambdaQueryWrapper<GoodsBatch> batchQw = new LambdaQueryWrapper<>();
        batchQw.likeRight(GoodsBatch::getBatchNo, batchPrefix)
               .orderByDesc(GoodsBatch::getBatchNo)
               .last("LIMIT 1");
        List<GoodsBatch> existingBatches = goodsBatchMapper.selectList(batchQw);
        int seq = 1;
        if (existingBatches != null && !existingBatches.isEmpty()) {
            String lastNo = existingBatches.get(0).getBatchNo();
            try { seq = Integer.parseInt(lastNo.substring(12)) + 1; } catch (Exception e) { /* keep seq */ }
        }

        // 2. 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < details.size(); i++) {
            PurchaseDTO.PurchaseDetailItem item = details.get(i);
            if (item.getCount() == null || item.getCount() <= 0) {
                throw new RuntimeException("商品数量必须大于0");
            }
            if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("商品价格不能为负数");
            }
            BigDecimal subtotal = item.getPrice().multiply(new BigDecimal(item.getCount()));
            item.setSubtotal(subtotal);
            totalAmount = totalAmount.add(subtotal);

            // 自动生成批次号：前缀 + 递增序号，保证全局唯一
            item.setBatchNo(batchPrefix + String.format("%02d", seq + i));
        }

        // 3. 设置采购单信息
        Purchase purchase = new Purchase();
        BeanUtils.copyProperties(purchaseDTO, purchase);

        if (purchase.getUserId() == null || purchase.getUserId() == 0) {
            throw new RuntimeException("采购员ID不能为空，请重新登录后再试");
        }

        purchase.setTotalAmount(totalAmount);
        purchase.setStatus(0); // 状态：待审核

        // 自动生成采购单号（如果前端未传）
        if (purchase.getPurchaseNo() == null || purchase.getPurchaseNo().isEmpty()) {
            purchase.setPurchaseNo(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        }

        // 4. 保存采购主表
        boolean saveResult = this.save(purchase);
        if (!saveResult) {
            throw new RuntimeException("采购单创建失败");
        }

        // 5. 保存采购明细（含批次号）
        List<PurchaseDetail> detailList = new ArrayList<>();
        for (PurchaseDTO.PurchaseDetailItem item : details) {
            PurchaseDetail detail = new PurchaseDetail();
            BeanUtils.copyProperties(item, detail);
            detail.setPurchaseId(purchase.getId());
            detail.setBatchNo(item.getBatchNo());
            detailList.add(detail);
        }

        for (PurchaseDetail detail : detailList) {
            purchaseDetailMapper.insert(detail);
        }

        return true;
    }

    /**
     * 店长审核采购单（状态 0→1）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(Integer purchaseId) {
        Purchase purchase = this.getById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }
        if (purchase.getStatus() != 0) {
            throw new RuntimeException("只有待审核状态的采购单才能审核，当前状态：" + purchase.getStatus());
        }
        purchase.setStatus(1); // 已审核待入库
        return this.updateById(purchase);
    }

    /**
     * 确认入库（店长选择仓库+填写实际入库数量 → 创建 goods_batch + 更新库存）
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean inbound(InboundDTO inboundDTO) {
        Integer purchaseId = inboundDTO.getPurchaseId();
        Integer storageId = inboundDTO.getStorageId();
        List<InboundDTO.InboundItem> items = inboundDTO.getItems();

        if (storageId == null) {
            throw new RuntimeException("请选择入库仓库");
        }
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("入库明细不能为空");
        }

        // 1. 查询采购单
        Purchase purchase = this.getById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }

        // 2. 检查状态：必须是"已审核待入库"
        if (purchase.getStatus() != 1) {
            throw new RuntimeException("只有已审核状态的采购单才能入库，当前状态：" + purchase.getStatus());
        }

        // 3. 查询采购明细
        LambdaQueryWrapper<PurchaseDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetail::getPurchaseId, purchaseId);
        List<PurchaseDetail> details = purchaseDetailMapper.selectList(queryWrapper);
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("采购明细不存在");
        }

        // 构建 detailId → PurchaseDetail 映射
        java.util.Map<Integer, PurchaseDetail> detailMap = new java.util.HashMap<>();
        for (PurchaseDetail d : details) {
            detailMap.put(d.getId(), d);
        }

        // 4. 遍历入库明细，创建 goods_batch 并更新库存
        for (InboundDTO.InboundItem item : items) {
            PurchaseDetail detail = detailMap.get(item.getDetailId());
            if (detail == null) {
                throw new RuntimeException("采购明细ID " + item.getDetailId() + " 不存在");
            }
            if (item.getActualCount() == null || item.getActualCount() <= 0) {
                throw new RuntimeException("入库数量必须大于0，明细ID：" + item.getDetailId());
            }
            if (item.getActualCount() > detail.getCount()) {
                throw new RuntimeException("入库数量不能超过采购数量，明细ID：" + item.getDetailId());
            }

            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID " + detail.getGoodsId() + " 不存在");
            }

            // 4.1 创建 goods_batch 批次库存记录
            GoodsBatch batch = new GoodsBatch();
            batch.setGoodsId(detail.getGoodsId());
            batch.setBatchNo(detail.getBatchNo());
            batch.setSupplierId(purchase.getSupplierId());
            batch.setStorageId(storageId);
            batch.setPurchasePrice(detail.getPrice());
            batch.setInitialCount(item.getActualCount());
            batch.setCurrentCount(item.getActualCount());
            batch.setPurchaseId(purchaseId);
            batch.setCreateTime(LocalDateTime.now());
            goodsBatchMapper.insert(batch);

            // 更新明细中的仓库ID
            detail.setStorageId(storageId);
            purchaseDetailMapper.updateById(detail);

            // 4.2 更新 goods.count + purchasePrice
            Integer currentCount = goods.getCount() != null ? goods.getCount() : 0;
            goods.setCount(currentCount + item.getActualCount());
            if (detail.getPrice() != null) {
                goods.setPurchasePrice(detail.getPrice());
            }
            goodsMapper.updateById(goods);

            // 4.3 记录出入库流水
            Record record = new Record();
            record.setGoods(detail.getGoodsId());
            record.setUserId(purchase.getUserId());
            record.setCount(item.getActualCount());
            record.setOperationType("采购入库");
            record.setRefOrderNum(purchase.getPurchaseNo() != null ? purchase.getPurchaseNo() : purchase.getId().toString());
            record.setAdminId(purchase.getUserId());
            record.setCreatetime(LocalDateTime.now());
            record.setStatus(1);
            recordService.save(record);
        }

        // 5. 更新采购单状态为已入库
        purchase.setStatus(2);
        this.updateById(purchase);

        return true;
    }

    /**
     * 采购退货（按批次FIFO扣减 goods_batch，同步更新 goods.count）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnGoods(Integer purchaseId) {
        // 1. 查询采购单
        Purchase purchase = this.getById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }

        // 2. 检查状态：必须是已入库
        if (purchase.getStatus() != 2) {
            throw new RuntimeException("只有已入库状态的采购单才能退货，当前状态：" + purchase.getStatus());
        }

        // 3. 查询采购明细
        LambdaQueryWrapper<PurchaseDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseDetail::getPurchaseId, purchaseId);
        List<PurchaseDetail> details = purchaseDetailMapper.selectList(queryWrapper);
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("采购明细不存在");
        }

        // 4. 遍历明细，按批次FIFO扣减
        for (PurchaseDetail detail : details) {
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品ID " + detail.getGoodsId() + " 不存在");
            }

            // 4.1 定位到该商品的该批次库存（同一批次的 goods_batch）
            LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
            batchQuery.eq(GoodsBatch::getGoodsId, detail.getGoodsId())
                      .eq(GoodsBatch::getBatchNo, detail.getBatchNo())
                      .gt(GoodsBatch::getCurrentCount, 0)
                      .orderByAsc(GoodsBatch::getCreateTime);
            List<GoodsBatch> batches = goodsBatchMapper.selectList(batchQuery);

            int remaining = detail.getCount(); // 需要退货的数量 = 采购时的数量
            for (GoodsBatch batch : batches) {
                if (remaining <= 0) break;
                int deduct = Math.min(batch.getCurrentCount(), remaining);
                batch.setCurrentCount(batch.getCurrentCount() - deduct);
                goodsBatchMapper.updateById(batch);
                remaining -= deduct;
            }

            if (remaining > 0) {
                throw new RuntimeException("商品【" + goods.getName() + "】批次库存不足，缺少：" + remaining);
            }

            // 4.2 更新 goods.count
            Integer currentCount = goods.getCount() != null ? goods.getCount() : 0;
            goods.setCount(currentCount - detail.getCount());
            goodsMapper.updateById(goods);

            // 4.3 记录出入库流水
            Record record = new Record();
            record.setGoods(detail.getGoodsId());
            record.setCount(-detail.getCount());
            record.setOperationType("采购退货");
            record.setRefOrderNum(purchase.getPurchaseNo() != null ? purchase.getPurchaseNo() : purchase.getId().toString());
            record.setAdminId(purchase.getUserId());
            record.setCreatetime(LocalDateTime.now());
            record.setStatus(1);
            recordService.save(record);
        }

        // 5. 更新采购单状态为已取消/已退货
        purchase.setStatus(3);
        this.updateById(purchase);

        return true;
    }
}
