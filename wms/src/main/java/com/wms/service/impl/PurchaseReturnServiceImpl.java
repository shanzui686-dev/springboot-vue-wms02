package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.*;
import com.wms.mapper.GoodsBatchMapper;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.PurchaseMapper;
import com.wms.mapper.PurchaseReturnMapper;
import com.wms.service.IPurchaseReturnService;
import com.wms.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PurchaseReturnServiceImpl extends ServiceImpl<PurchaseReturnMapper, PurchaseReturn>
        implements IPurchaseReturnService {

    @Autowired
    private PurchaseReturnMapper purchaseReturnMapper;
    @Autowired
    private PurchaseMapper purchaseMapper;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private GoodsBatchMapper goodsBatchMapper;
    @Autowired
    private IRecordService recordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseReturn submit(PurchaseReturnDTO dto) {
        // 1. 校验
        if (dto.getReturnCount() == null || dto.getReturnCount() <= 0) {
            throw new RuntimeException("退货数量必须大于0");
        }

        // 查询采购单状态
        Purchase purchase = purchaseMapper.selectById(dto.getPurchaseId());
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }

        GoodsBatch batch = null;
        // 已入库的采购单需要校验批次库存
        if (purchase.getStatus() == 2) {
            // 按 batchNo + goodsId 查 goods_batch
            if (dto.getBatchNo() != null && dto.getGoodsId() != null) {
                LambdaQueryWrapper<GoodsBatch> qw = new LambdaQueryWrapper<>();
                qw.eq(GoodsBatch::getBatchNo, dto.getBatchNo())
                  .eq(GoodsBatch::getGoodsId, dto.getGoodsId());
                batch = goodsBatchMapper.selectOne(qw);
            }
            if (batch == null) {
                throw new RuntimeException("批次库存记录不存在，批次号：" + dto.getBatchNo());
            }
            dto.setBatchId(batch.getId());
            if (batch.getCurrentCount() < dto.getReturnCount()) {
                throw new RuntimeException("批次【" + dto.getBatchNo() + "】剩余数量不足，当前剩余："
                    + batch.getCurrentCount() + "，退货数量：" + dto.getReturnCount());
            }
        }
        // 已审核待入库的采购单不需要校验批次库存

        // 生成退货单号
        String returnNo = "PR" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        // 2. 创建退货单
        PurchaseReturn pr = new PurchaseReturn();
        pr.setReturnNo(returnNo);
        pr.setPurchaseId(dto.getPurchaseId());
        pr.setGoodsId(dto.getGoodsId());
        pr.setBatchId(dto.getBatchId());
        pr.setBatchNo(dto.getBatchNo());
        pr.setReturnCount(dto.getReturnCount());
        pr.setReason(dto.getReason());
        pr.setCreatorId(dto.getCreatorId());
        pr.setStatus(1); // 待审核
        pr.setCreateTime(LocalDateTime.now());
        this.save(pr);

        // 3. 更新采购单状态为"待退回"
        purchase.setStatus(5); // 待退回
        purchaseMapper.updateById(purchase);

        return pr;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Integer returnId, Integer auditorId) {
        PurchaseReturn pr = this.getById(returnId);
        if (pr == null) throw new RuntimeException("退货单不存在");
        if (pr.getStatus() != 1) throw new RuntimeException("只有待审核状态的退货单才能审核");

        // 查询采购单状态
        Purchase purchase = purchaseMapper.selectById(pr.getPurchaseId());
        if (purchase == null) throw new RuntimeException("采购单不存在");

        // 已入库的采购单需要扣减批次库存
        if (purchase.getStatus() == 2) {
            // 1. FIFO 扣减 goods_batch（按 batchId 查，失败则按 batchNo+goodsId 兜底）
            GoodsBatch batch = goodsBatchMapper.selectById(pr.getBatchId());
            if (batch == null && pr.getBatchNo() != null && pr.getGoodsId() != null) {
                LambdaQueryWrapper<GoodsBatch> qw = new LambdaQueryWrapper<>();
                qw.eq(GoodsBatch::getBatchNo, pr.getBatchNo())
                  .eq(GoodsBatch::getGoodsId, pr.getGoodsId());
                batch = goodsBatchMapper.selectOne(qw);
            }
            if (batch == null) throw new RuntimeException("批次库存记录不存在");
            if (batch.getCurrentCount() < pr.getReturnCount()) {
                throw new RuntimeException("批次库存不足，当前剩余：" + batch.getCurrentCount());
            }
            batch.setCurrentCount(batch.getCurrentCount() - pr.getReturnCount());
            goodsBatchMapper.updateById(batch);

            // 2. 同步 goods.count
            Goods goods = goodsMapper.selectById(pr.getGoodsId());
            if (goods == null) throw new RuntimeException("商品不存在");
            goods.setCount(goods.getCount() - pr.getReturnCount());
            goodsMapper.updateById(goods);
        }
        // 已审核待入库的采购单不需要扣减库存，只需记录流水

        // 3. 记录流水
        Record record = new Record();
        record.setGoods(pr.getGoodsId());
        record.setUserId(pr.getCreatorId());
        record.setCount(-pr.getReturnCount());
        record.setOperationType("采购退货");
        record.setRefOrderNum(pr.getReturnNo());
        record.setAdminId(auditorId);
        record.setCreatetime(LocalDateTime.now());
        record.setStatus(1);
        recordService.save(record);

        // 4. 更新退货单状态
        pr.setStatus(2); // 已完成
        pr.setAuditorId(auditorId);
        pr.setAuditTime(LocalDateTime.now());
        this.updateById(pr);

        // 5. 更新采购单状态为"已退回"
        purchase.setStatus(6); // 已退回
        purchaseMapper.updateById(purchase);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reject(Integer returnId, Integer auditorId) {
        PurchaseReturn pr = this.getById(returnId);
        if (pr == null) throw new RuntimeException("退货单不存在");
        if (pr.getStatus() != 1) throw new RuntimeException("只有待审核状态的退货单才能操作");
        pr.setStatus(3); // 不退回
        pr.setAuditorId(auditorId);
        pr.setAuditTime(LocalDateTime.now());
        this.updateById(pr);

        // 更新采购单状态为"不退回"
        Purchase purchase = purchaseMapper.selectById(pr.getPurchaseId());
        if (purchase != null) {
            purchase.setStatus(7); // 不退回
            purchaseMapper.updateById(purchase);
        }

        return true;
    }

    @Override
    public IPage<PurchaseReturn> listPage(Integer pagenum, Integer pagesize, String returnNo, Integer status) {
        Page<PurchaseReturn> page = new Page<>(pagenum, pagesize);
        IPage<PurchaseReturnVO> result = purchaseReturnMapper.selectPage(page, returnNo, status);
        Page<PurchaseReturn> converted = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        converted.setRecords(result.getRecords().stream().map(v -> (PurchaseReturn) v).collect(java.util.stream.Collectors.toList()));
        return converted;
    }
}