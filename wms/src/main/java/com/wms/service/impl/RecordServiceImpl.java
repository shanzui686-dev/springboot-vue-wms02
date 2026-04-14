package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.mapper.RecordMapper;
import com.wms.service.IGoodsService;
import com.wms.service.IRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-04-01
 */
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements IRecordService {

    @Autowired
    private IGoodsService goodsService;

    /**
     * 确认出库记录（管理员审核通过）
     * 事务控制：同时更新记录状态和扣减库存
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmRecord(Integer recordId) {
        // 1. 查询记录
        Record record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        
        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已处理，无法重复审核");
        }
        
        // 2. 获取数量
        Integer count = record.getCount();
        if (count == null || count == 0) {
            throw new RuntimeException("数量异常");
        }
        
        // 3. 查询商品
        Goods goods = goodsService.getById(record.getGoods());
        if (goods == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 4. 更新库存
        int currentCount = goods.getCount();
        int newCount = currentCount + count; // count为正数表示入库，负数表示出库
        
        // 如果是出库（负数），检查库存是否充足
        if (count < 0 && currentCount < Math.abs(count)) {
            throw new RuntimeException("库存不足！当前库存：" + currentCount);
        }
        
        goods.setCount(newCount);
        boolean goodsUpdated = goodsService.updateById(goods);
        if (!goodsUpdated) {
            throw new RuntimeException("库存更新失败");
        }
        
        // 5. 更新记录状态为已完成
        LambdaUpdateWrapper<Record> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Record::getId, recordId)
                    .set(Record::getStatus, 1);
        
        return this.update(updateWrapper);
    }

    /**
     * 拒绝出库记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectRecord(Integer recordId) {
        // 1. 查询记录
        Record record = this.getById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        
        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已处理，无法重复审核");
        }
        
        // 2. 更新记录状态为已拒绝
        LambdaUpdateWrapper<Record> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Record::getId, recordId)
                    .set(Record::getStatus, 2);
        
        return this.update(updateWrapper);
    }
}
