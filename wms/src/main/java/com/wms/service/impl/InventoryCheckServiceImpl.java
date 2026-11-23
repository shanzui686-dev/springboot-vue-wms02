package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.service.IInventoryCheckService;
import com.wms.service.IGoodsService;
import com.wms.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 库存盘点 服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
@Service
public class InventoryCheckServiceImpl extends ServiceImpl<InventoryCheckMapper, InventoryCheck> implements IInventoryCheckService {

    @Autowired
    private InventoryCheckMapper inventoryCheckMapper;

    @Autowired
    private InventoryCheckDetailMapper inventoryCheckDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private GoodsBatchMapper goodsBatchMapper;

    @Autowired
    private IRecordService recordService;

    /**
     * 创建盘点单
     * 逻辑：生成CHK单号 -> 插入主表(状态0) -> 查询该仓库所有商品 -> 将当前库存作为账面库存批量插入明细表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createCheck(InventoryCheck check) {
        // 1. 生成盘点单号：CHK + 年月日 + 6位随机数
        String checkNo = generateCheckNo();
        check.setCheckNo(checkNo);
        check.setStatus(0); // 状态0：待录入
        check.setCreateTime(LocalDateTime.now());
        // 整个系统只有一个仓库（主仓库），未指定时默认使用主仓库
        if (check.getStorageId() == null) {
            check.setStorageId(1);
        }

        // 2. 插入主表
        this.save(check);
        Integer checkId = check.getId();

        // 3. 查询所有商品（整个系统只有一个仓库，库存快照覆盖全部商品）
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        List<Goods> goodsList = goodsMapper.selectList(queryWrapper);

        // 4. 批量插入明细表（将当前库存作为账面库存）
        if (goodsList != null && !goodsList.isEmpty()) {
            List<InventoryCheckDetail> detailList = new ArrayList<>();
            for (Goods goods : goodsList) {
                InventoryCheckDetail detail = new InventoryCheckDetail();
                detail.setCheckId(checkId);
                detail.setGoodsId(goods.getId());
                int availableStock = goods.getCount() - (goods.getReservedCount() != null ? goods.getReservedCount() : 0);
                detail.setExpectedCount(availableStock); // 可用库存作为账面库存
                detail.setActualCount(0); // 初始实盘为0
                detail.setDiffCount(-availableStock); // 初始差异 = 0 - 可用库存
                detailList.add(detail);
            }

            // 批量插入明细
            for (InventoryCheckDetail detail : detailList) {
                inventoryCheckDetailMapper.insert(detail);
            }
        }

        return true;
    }

    /**
     * 录入实盘数据
     * 逻辑：更新明细表的实盘数量 -> 自动计算差异 -> 主表状态改为1(待审核)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean inputActualData(CheckInputDTO dto) {
        Integer checkId = dto.getCheckId();

        // 1. 校验盘点单状态
        InventoryCheck check = this.getById(checkId);
        if (check == null) {
            throw new RuntimeException("盘点单不存在");
        }
        if (check.getStatus() != 0) {
            throw new RuntimeException("盘点单状态不正确，无法录入");
        }

        // 2. 遍历明细列表，更新实盘数据
        List<CheckInputDTO.CheckDetailInputDTO> details = dto.getDetails();
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("实盘数据不能为空");
        }

        for (CheckInputDTO.CheckDetailInputDTO detailDTO : details) {
            InventoryCheckDetail detail = inventoryCheckDetailMapper.selectById(detailDTO.getDetailId());
            if (detail == null) {
                throw new RuntimeException("明细不存在");
            }
            // 更新实盘数量（会自动计算差异）
            detail.setActualCount(detailDTO.getActualCount());
            inventoryCheckDetailMapper.updateById(detail);
        }

        // 3. 将主表状态更新为1（待审核）
        check.setStatus(1);
        this.updateById(check);

        return true;
    }

    /**
     * 审核盘点单
     * 逻辑：主表状态改为2(已完成) -> 遍历明细调整商品库存 -> 插入流水记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditCheck(Integer checkId) {
        // 1. 校验盘点单状态
        InventoryCheck check = this.getById(checkId);
        if (check == null) {
            throw new RuntimeException("盘点单不存在");
        }
        if (check.getStatus() != 1) {
            throw new RuntimeException("盘点单状态不正确，无法审核");
        }

        // 2. 查询该盘点单的所有明细
        LambdaQueryWrapper<InventoryCheckDetail> detailQuery = new LambdaQueryWrapper<>();
        detailQuery.eq(InventoryCheckDetail::getCheckId, checkId);
        List<InventoryCheckDetail> detailList = inventoryCheckDetailMapper.selectList(detailQuery);

        // 3. 遍历明细，调整商品库存
        for (InventoryCheckDetail detail : detailList) {
            Integer diffCount = detail.getDiffCount();
            // 如果差异为0，不需要处理
            if (diffCount == null || diffCount == 0) {
                continue;
            }

            // 查询商品信息
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods == null) {
                throw new RuntimeException("商品不存在，商品ID：" + detail.getGoodsId());
            }

            // 调整库存：盘盈增加、盘亏扣减，同步 goods_batch
            int remaining = diffCount;
            StringBuilder batchDetail = new StringBuilder();

            if (remaining < 0) {
                // 盘亏 — FIFO 逐批扣减
                int toDeduct = -remaining;
                LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
                batchQuery.eq(GoodsBatch::getGoodsId, goods.getId())
                          .gt(GoodsBatch::getCurrentCount, 0)
                          .orderByAsc(GoodsBatch::getCreateTime);
                List<GoodsBatch> batches = goodsBatchMapper.selectList(batchQuery);
                for (GoodsBatch batch : batches) {
                    if (toDeduct <= 0) break;
                    int deduct = Math.min(batch.getCurrentCount(), toDeduct);
                    batch.setCurrentCount(batch.getCurrentCount() - deduct);
                    goodsBatchMapper.updateById(batch);
                    batchDetail.append("批次").append(batch.getBatchNo())
                               .append("扣").append(deduct)
                               .append("(进价").append(batch.getPurchasePrice()).append(");");
                    toDeduct -= deduct;
                }
                if (toDeduct > 0) {
                    throw new RuntimeException("批次库存不足，商品：" + goods.getName() + "，缺口：" + toDeduct);
                }
            } else if (remaining > 0) {
                // 盘盈 — 追加到最近一个批次
                LambdaQueryWrapper<GoodsBatch> batchQuery = new LambdaQueryWrapper<>();
                batchQuery.eq(GoodsBatch::getGoodsId, goods.getId())
                          .orderByDesc(GoodsBatch::getCreateTime)
                          .last("LIMIT 1");
                List<GoodsBatch> batches = goodsBatchMapper.selectList(batchQuery);
                if (batches != null && !batches.isEmpty()) {
                    GoodsBatch latest = batches.get(0);
                    latest.setCurrentCount(latest.getCurrentCount() + remaining);
                    goodsBatchMapper.updateById(latest);
                    batchDetail.append("批次").append(latest.getBatchNo())
                               .append("增").append(remaining)
                               .append("(进价").append(latest.getPurchasePrice()).append(")");
                } else {
                    batchDetail.append("无批次记录，仅更新总库存");
                }
            }

            Integer newCount = goods.getCount() + diffCount;
            if (newCount < 0) {
                throw new RuntimeException("商品库存不足，商品：" + goods.getName());
            }
            goods.setCount(newCount);
            goodsService.updateById(goods);

            // 4. 插入流水记录（操作类型：盘点盈亏）
            Record record = new Record();
            record.setGoods(detail.getGoodsId());
            record.setUserId(null); // 盘点盈亏无用户操作
            record.setAdminId(check.getCreatorId()); // 创建人作为操作人
            record.setCount(diffCount); // 差异数量
            record.setCreatetime(LocalDateTime.now());
            record.setOperationType("盘点盈亏");
            record.setRefOrderNum(check.getCheckNo()); // 关联盘点单号
            record.setRemark("盘点差异调整，实盘：" + detail.getActualCount()
                    + "，账面：" + detail.getExpectedCount()
                    + "，差异：" + diffCount
                    + "。" + batchDetail);
            record.setStatus(1); // 已完成
            recordService.save(record);
        }

        // 5. 更新主表状态为2（已完成）
        check.setStatus(2);
        this.updateById(check);

        return true;
    }

    /**
     * 分页查询盘点单（连表查询仓库名称）
     */
    @Override
    public IPage<InventoryCheckVO> listPage(Integer pageNum, Integer pageSize, String checkNo, Integer status) {
        Page<InventoryCheckVO> page = new Page<>(pageNum, pageSize);

        // 使用原生SQL连表查询（通过BaseMapper的selectPage配合自定义SQL）
        IPage<InventoryCheckVO> result = inventoryCheckMapper.selectCheckPage(page, checkNo, status);

        return result;
    }

    /**
     * 生成盘点单号：CHK + 年月日 + 6位随机数
     */
    private String generateCheckNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = (int) (Math.random() * 900000) + 100000; // 6位随机数
        return "CHK" + dateStr + random;
    }
}
