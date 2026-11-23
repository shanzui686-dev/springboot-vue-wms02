package com.wms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.RequireRole;
import com.wms.common.Result;
import com.wms.entity.CheckInputDTO;
import com.wms.entity.Goods;
import com.wms.entity.InventoryCheck;
import com.wms.entity.InventoryCheckDetail;
import com.wms.entity.InventoryCheckVO;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.InventoryCheckDetailMapper;
import com.wms.service.IInventoryCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 库存盘点 前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-05-10
 */
@RestController
@RequestMapping("/inventoryCheck")
@RequireRole({0, 1}) // 店长+库存管理员可进行库存盘点
public class InventoryCheckController {

    @Autowired
    private IInventoryCheckService inventoryCheckService;

    @Autowired
    private InventoryCheckDetailMapper inventoryCheckDetailMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    /**
     * 创建盘点单
     * 逻辑：生成CHK单号 -> 插入主表(状态0:待录入) -> 查询该仓库所有商品 -> 批量插入明细(库存快照)
     */
    @Log("创建盘点单")
    @PostMapping("/create")
    public Result create(@RequestBody InventoryCheck check) {
        try {
            return inventoryCheckService.createCheck(check) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 录入实盘数据
     * 逻辑：更新明细表实盘数量 -> 自动计算差异 -> 主表状态改为1(待审核)
     */
    @Log("录入实盘数据")
    @PostMapping("/input")
    public Result input(@RequestBody CheckInputDTO dto) {
        try {
            return inventoryCheckService.inputActualData(dto) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 审核盘点单
     * 逻辑：@Transactional -> 主表状态改为2(已完成) -> 遍历明细调整商品库存 -> 插入"盘点盈亏"流水记录
     */
    @Log("审核盘点单")
    @PostMapping("/audit")
    public Result audit(@RequestParam Integer checkId) {
        try {
            return inventoryCheckService.auditCheck(checkId) ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 分页查询盘点单
     * 支持按状态、单号模糊查询，连表查出仓库名称
     */
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query) {
        HashMap param = query.getParam();
        
        String checkNo = (String) param.get("checkNo");
        Object statusObj = param.get("status");
        
        Integer status = statusObj != null ? Integer.parseInt(statusObj.toString()) : null;

        IPage<InventoryCheckVO> result = inventoryCheckService.listPage(
            query.getPagenum(),
            query.getPagesize(),
            checkNo,
            status
        );

        return Result.suc(result.getRecords(), result.getTotal());
    }

    /**
     * 查询盘点明细列表（含商品名）
     */
    @GetMapping("/detail/list")
    public Result getDetailList(@RequestParam Integer checkId) {
        LambdaQueryWrapper<InventoryCheckDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InventoryCheckDetail::getCheckId, checkId);
        List<InventoryCheckDetail> details = inventoryCheckDetailMapper.selectList(queryWrapper);

        // 拼上商品名称
        List<HashMap<String, Object>> result = new java.util.ArrayList<>();
        for (InventoryCheckDetail d : details) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("checkId", d.getCheckId());
            map.put("goodsId", d.getGoodsId());
            map.put("expectedCount", d.getExpectedCount());
            map.put("actualCount", d.getActualCount());
            map.put("diffCount", d.getDiffCount());
            map.put("remark", d.getRemark());
            Goods goods = goodsMapper.selectById(d.getGoodsId());
            map.put("goodsName", goods != null ? goods.getName() : "未知商品");
            map.put("barcode", goods != null ? goods.getBarcode() : "");
            result.add(map);
        }
        return Result.suc(result);
    }
}