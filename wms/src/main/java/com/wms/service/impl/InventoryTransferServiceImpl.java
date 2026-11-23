package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.service.IInventoryTransferService;
import com.wms.service.IRecordService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InventoryTransferServiceImpl extends ServiceImpl<InventoryTransferMapper, InventoryTransfer> implements IInventoryTransferService {

    @Autowired
    private InventoryTransferMapper inventoryTransferMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;

    @Autowired
    private IRecordService recordService;

    @Autowired
    private StorageMapper storageMapper;

    /**
     * 创建调拨申请（仅生成单据，不执行库存变动）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createTransfer(TransferDTO dto) {
        if (dto.getFromStorageId() == null || dto.getToStorageId() == null) {
            throw new RuntimeException("调出仓库和调入仓库不能为空");
        }
        if (dto.getFromStorageId().equals(dto.getToStorageId())) {
            throw new RuntimeException("调出仓库和调入仓库不能相同");
        }
        if (dto.getGoodsId() == null || dto.getCount() == null || dto.getCount() <= 0) {
            throw new RuntimeException("商品和数量不能为空");
        }

        // 校验商品是否存在
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        // 校验调出仓批次库存是否充足
        LambdaQueryWrapper<GoodsBatch> checkQuery = new LambdaQueryWrapper<>();
        checkQuery.eq(GoodsBatch::getGoodsId, dto.getGoodsId())
                  .eq(GoodsBatch::getStorageId, dto.getFromStorageId())
                  .gt(GoodsBatch::getCurrentCount, 0);
        Integer available = goodsBatchMapper.selectList(checkQuery).stream()
                .mapToInt(GoodsBatch::getCurrentCount).sum();
        if (available < dto.getCount()) {
            throw new RuntimeException("调出仓库批次库存不足，可用:" + available + "，需要:" + dto.getCount());
        }

        // 生成调拨单
        InventoryTransfer transfer = new InventoryTransfer();
        transfer.setTransferNo(generateTransferNo());
        transfer.setFromStorageId(dto.getFromStorageId());
        transfer.setToStorageId(dto.getToStorageId());
        transfer.setGoodsId(dto.getGoodsId());
        transfer.setCount(dto.getCount());
        transfer.setStatus(0); // 待审核
        transfer.setCreatorId(dto.getCreatorId());
        transfer.setCreateTime(LocalDateTime.now());
        transfer.setRemark(dto.getRemark());

        return this.save(transfer);
    }

    /**
     * 审核通过 — 执行实际库存调拨
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditTransfer(Integer transferId, Integer auditorId) {
        InventoryTransfer transfer = this.getById(transferId);
        if (transfer == null) {
            throw new RuntimeException("调拨单不存在");
        }
        if (transfer.getStatus() != 0) {
            throw new RuntimeException("调拨单状态不正确，无法审核");
        }

        Integer goodsId = transfer.getGoodsId();
        Integer fromStorageId = transfer.getFromStorageId();
        Integer toStorageId = transfer.getToStorageId();
        int transferCount = transfer.getCount();

        // 1. 校验商品存在
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }

        // 2. FIFO 扣减调出仓 goods_batch（同一商品，按仓库区分批次）
        List<GoodsBatch> deductedBatches = new ArrayList<>();
        int remaining = transferCount;
        LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
        batchQuery.eq(GoodsBatch::getGoodsId, goodsId)
                  .eq(GoodsBatch::getStorageId, fromStorageId)
                  .gt(GoodsBatch::getCurrentCount, 0)
                  .orderByAsc(GoodsBatch::getCreateTime);
        List<GoodsBatch> sourceBatches = goodsBatchMapper.selectList(batchQuery);
        for (GoodsBatch batch : sourceBatches) {
            if (remaining <= 0) break;
            int deduct = Math.min(batch.getCurrentCount(), remaining);
            batch.setCurrentCount(batch.getCurrentCount() - deduct);
            goodsBatchMapper.updateById(batch);

            GoodsBatch movedBatch = new GoodsBatch();
            movedBatch.setGoodsId(goodsId);
            movedBatch.setBatchNo(batch.getBatchNo());
            movedBatch.setSupplierId(batch.getSupplierId());
            movedBatch.setPurchasePrice(batch.getPurchasePrice());
            movedBatch.setInitialCount(deduct);
            movedBatch.setCurrentCount(deduct);
            movedBatch.setPurchaseId(batch.getPurchaseId());
            movedBatch.setCreateTime(batch.getCreateTime());
            deductedBatches.add(movedBatch);

            remaining -= deduct;
        }
        if (remaining > 0) {
            throw new RuntimeException("调出仓批次库存不足，缺口:" + remaining);
        }

        // 3. 调入仓批次处理：同批次号叠加，无则新增（同一 goodsId，不同 storageId）
        StringBuilder batchDetail = new StringBuilder();
        for (GoodsBatch db : deductedBatches) {
            LambdaQueryWrapper<GoodsBatch> existQuery = new LambdaQueryWrapper<>();
            existQuery.eq(GoodsBatch::getGoodsId, goodsId)
                      .eq(GoodsBatch::getStorageId, toStorageId)
                      .eq(GoodsBatch::getBatchNo, db.getBatchNo());
            GoodsBatch existBatch = goodsBatchMapper.selectOne(existQuery);
            if (existBatch != null) {
                existBatch.setCurrentCount(existBatch.getCurrentCount() + db.getInitialCount());
                existBatch.setInitialCount(existBatch.getInitialCount() + db.getInitialCount());
                goodsBatchMapper.updateById(existBatch);
            } else {
                db.setStorageId(toStorageId);
                goodsBatchMapper.insert(db);
            }
            batchDetail.append("批次").append(db.getBatchNo())
                       .append("调").append(db.getInitialCount())
                       .append("(进价").append(db.getPurchasePrice()).append(");");
        }

        // 4. 更新调拨单状态
        transfer.setStatus(1);
        transfer.setAuditorId(auditorId);
        transfer.setAuditTime(LocalDateTime.now());
        this.updateById(transfer);

        // 5. 双向 Record 记账
        Record outRecord = new Record();
        outRecord.setGoods(goodsId);
        outRecord.setUserId(null);
        outRecord.setAdminId(auditorId);
        outRecord.setCount(-transferCount);
        outRecord.setCreatetime(LocalDateTime.now());
        outRecord.setOperationType("库存调拨");
        outRecord.setRefOrderNum(transfer.getTransferNo());
        outRecord.setRemark("从仓库" + fromStorageId + "调出至仓库" + toStorageId + "。" + batchDetail);
        outRecord.setStatus(1);
        recordService.save(outRecord);

        Record inRecord = new Record();
        inRecord.setGoods(goodsId);
        inRecord.setUserId(null);
        inRecord.setAdminId(auditorId);
        inRecord.setCount(transferCount);
        inRecord.setCreatetime(LocalDateTime.now());
        inRecord.setOperationType("库存调拨");
        inRecord.setRefOrderNum(transfer.getTransferNo());
        inRecord.setRemark("从仓库" + fromStorageId + "调入。" + batchDetail);
        inRecord.setStatus(1);
        recordService.save(inRecord);

        return true;
    }

    /**
     * 审核拒绝
     */
    @Override
    public boolean rejectTransfer(Integer transferId, Integer auditorId) {
        InventoryTransfer transfer = this.getById(transferId);
        if (transfer == null) {
            throw new RuntimeException("调拨单不存在");
        }
        if (transfer.getStatus() != 0) {
            throw new RuntimeException("调拨单状态不正确，无法审核");
        }

        transfer.setStatus(2);
        transfer.setAuditorId(auditorId);
        transfer.setAuditTime(LocalDateTime.now());
        return this.updateById(transfer);
    }

    @Override
    public IPage<TransferVO> listPage(Integer pageNum, Integer pageSize, String transferNo, Integer goodsId, Integer status) {
        Page<InventoryTransfer> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryTransfer> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(transferNo) && !"null".equals(transferNo)) {
            queryWrapper.like(InventoryTransfer::getTransferNo, transferNo);
        }
        if (goodsId != null) {
            queryWrapper.eq(InventoryTransfer::getGoodsId, goodsId);
        }
        if (status != null) {
            queryWrapper.eq(InventoryTransfer::getStatus, status);
        }
        queryWrapper.orderByDesc(InventoryTransfer::getCreateTime);
        IPage<InventoryTransfer> rawPage = this.page(page, queryWrapper);

        List<InventoryTransfer> records = rawPage.getRecords();
        if (records.isEmpty()) {
            Page<TransferVO> emptyPage = new Page<>(pageNum, pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        // 批量查商品名
        List<Integer> goodsIds = records.stream().map(InventoryTransfer::getGoodsId).distinct().collect(Collectors.toList());
        Map<Integer, String> goodsNameMap = goodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, Goods::getName));

        // 批量查仓库名
        List<Integer> storageIds = new ArrayList<>();
        records.forEach(r -> {
            if (r.getFromStorageId() != null) storageIds.add(r.getFromStorageId());
            if (r.getToStorageId() != null) storageIds.add(r.getToStorageId());
        });
        Map<Integer, String> storageNameMap = storageMapper.selectBatchIds(
                storageIds.stream().distinct().collect(Collectors.toList())).stream()
                .collect(Collectors.toMap(Storage::getId, Storage::getName));

        // 转换 VO
        List<TransferVO> voList = records.stream().map(r -> {
            TransferVO vo = new TransferVO();
            BeanUtils.copyProperties(r, vo);
            vo.setGoodsName(goodsNameMap.getOrDefault(r.getGoodsId(), ""));
            vo.setFromStorageName(storageNameMap.getOrDefault(r.getFromStorageId(), ""));
            vo.setToStorageName(storageNameMap.getOrDefault(r.getToStorageId(), ""));
            return vo;
        }).collect(Collectors.toList());

        Page<TransferVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotal(rawPage.getTotal());
        return voPage;
    }

    private String generateTransferNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = (int) (Math.random() * 900000) + 100000;
        return "DB" + dateStr + random;
    }
}
