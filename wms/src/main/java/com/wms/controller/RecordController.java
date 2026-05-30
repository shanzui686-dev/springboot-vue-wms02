package com.wms.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Log;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.entity.RecordExportVO;
import com.wms.entity.RecordRes;
import com.wms.mapper.RecordMapper;
import com.wms.service.IGoodsService;
import com.wms.service.IRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author wms
 * @since 2026-04-01
 */
@RestController
@RequestMapping("/record")
public class RecordController {
    @Autowired
    private IRecordService recordService;

    @Autowired
    private IGoodsService goodsService;

    @Autowired
    private RecordMapper recordMapper;
    @PostMapping("/listPageCC")
    public Result listPageCC(@RequestBody QueryPageParam query ){
        HashMap param=query.getParam();
        String name=(String) param.get("name");
        Object goodstypeObj = param.get("goodstype");
        Object storageObj = param.get("storage");
        Object roleIdObj = param.get("roleId");
        Object userIdObj = param.get("userId");
        String operationType = (String) param.get("operationType");
        String startDate = (String) param.get("startDate");
        String endDate = (String) param.get("endDate");
        
        String goodstype = goodstypeObj != null ? goodstypeObj.toString() : null;
        String storage = storageObj != null ? storageObj.toString() : null;
        String roleId = roleIdObj != null ? roleIdObj.toString() : null;
        String userId = userIdObj != null ? userIdObj.toString() : null;
        
        Page<Record> page=new Page();
        page.setCurrent(query.getPagenum());
        page.setSize(query.getPagesize());
        
        QueryWrapper<Record> queryWrapper=new QueryWrapper<>();
        // XML 中已使用 LEFT JOIN 关联表，此处不再添加 apply 条件，避免将 LEFT JOIN 降级为 INNER JOIN
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            queryWrapper.like("b.name", name);
        }
        if(StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)){
            queryWrapper.eq("d.id",Integer.parseInt(goodstype));
        }
        if(StringUtils.isNotBlank(storage) && !"null".equals(storage)){
            queryWrapper.eq("c.id",Integer.parseInt(storage));
        }
        if(StringUtils.isNotBlank(operationType) && !"null".equals(operationType)){
            queryWrapper.eq("a.operation_type", operationType);
        }
        // 时间范围查询：使用ge和le对create_time进行区间过滤
        if(StringUtils.isNotBlank(startDate) && !"null".equals(startDate)){
            queryWrapper.ge("a.createtime", startDate);
        }
        if(StringUtils.isNotBlank(endDate) && !"null".equals(endDate)){
            queryWrapper.le("a.createtime", endDate);
        }
        if("2".equals(roleId)){
            queryWrapper.eq("a.userId", userId);
        }

        queryWrapper.orderByDesc("a.createtime");

        // 使用自定义查询
        IPage<RecordRes> result=recordMapper.pageCC(page,queryWrapper);
        return Result.suc(result.getRecords(),result.getTotal());
    }
    //新增
    @Log("入库申请")
    @PostMapping("/save")
    public Result save(@RequestBody Record record){
        System.out.println("========== 入库记录 ==========");
        System.out.println("完整记录: " + record);
        System.out.println("商品ID: " + record.getGoods());
        System.out.println("申请人ID (userId): " + record.getUserId());
        System.out.println("操作人ID (adminId): " + record.getAdminId());
        System.out.println("数量: " + record.getCount());
        System.out.println("备注: " + record.getRemark());
        
        if(record.getGoods() == null){
            return Result.fail("商品ID不能为空");
        }
        
        Goods goods = goodsService.getById(record.getGoods());
        if(goods == null){
            return Result.fail("商品不存在");
        }
        
        System.out.println("原数量: " + goods.getCount());
        System.out.println("入库数量: " + record.getCount());
        
        int num = goods.getCount() + record.getCount();
        goods.setCount(num);
        
        boolean updated = goodsService.updateById(goods);
        System.out.println("更新结果: " + updated);
        System.out.println("新数量: " + goods.getCount());
        
        // 设置操作时间为当前时间
        record.setCreatetime(LocalDateTime.now());
        
        // 设置状态为待审核(0)
        record.setStatus(0);
        
        return recordService.save(record)?Result.suc():Result.fail();
    }
    
    // 出库申请（仅插入记录，不扣减库存）
    @Log("出库申请")
    @PostMapping("/out")
    public Result out(@RequestBody Record record){
        System.out.println("========== 出库申请 ==========");
        System.out.println("完整记录: " + record);
        System.out.println("商品ID: " + record.getGoods());
        System.out.println("申请人ID (userId): " + record.getUserId());
        System.out.println("操作人ID (adminId): " + record.getAdminId());
        System.out.println("数量: " + record.getCount());
        System.out.println("备注: " + record.getRemark());
        
        if(record.getGoods() == null){
            return Result.fail("商品ID不能为空");
        }
        
        if(record.getCount() == null || record.getCount() <= 0){
            return Result.fail("出库数量必须大于0");
        }
        
        Goods goods = goodsService.getById(record.getGoods());
        if(goods == null){
            return Result.fail("商品不存在");
        }
        
        System.out.println("当前库存: " + goods.getCount());
        System.out.println("申请出库数量: " + record.getCount());
        
        // 检查库存是否充足（仅做预检查，不实际扣减）
        if(goods.getCount() < record.getCount()){
            return Result.fail("库存不足！当前库存：" + goods.getCount());
        }
        
        // 设置操作时间为当前时间，出库数量存为负数
        record.setCount(-record.getCount());
        record.setCreatetime(LocalDateTime.now());
        
        // 设置状态为待审核(0)
        record.setStatus(0);
        
        return recordService.save(record)?Result.suc():Result.fail();
    }
    
    // 确认出库（管理员审核通过）
    @Log("确认出库")
    @PostMapping("/confirm")
    public Result confirm(@RequestBody HashMap<String, Object> params){
        Integer recordId = (Integer) params.get("recordId");
        
        if(recordId == null){
            return Result.fail("记录ID不能为空");
        }
        
        try {
            boolean success = recordService.confirmRecord(recordId);
            return success ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
    
    // 拒绝出库（管理员审核拒绝）
    @Log("拒绝出库")
    @PostMapping("/reject")
    public Result reject(@RequestBody HashMap<String, Object> params){
        Integer recordId = (Integer) params.get("recordId");
        
        if(recordId == null){
            return Result.fail("记录ID不能为空");
        }
        
        try {
            boolean success = recordService.rejectRecord(recordId);
            return success ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }
    
    // 数据导出接口
    @PostMapping("/export")
    public void export(@RequestBody QueryPageParam query, HttpServletResponse response) throws IOException {
        HashMap param = query.getParam();
        String name = (String) param.get("name");
        Object goodstypeObj = param.get("goodstype");
        Object storageObj = param.get("storage");
        String operationType = (String) param.get("operationType");
        String startDate = (String) param.get("startDate");
        String endDate = (String) param.get("endDate");
        
        String goodstype = goodstypeObj != null ? goodstypeObj.toString() : null;
        String storage = storageObj != null ? storageObj.toString() : null;
        
        // 构建查询条件（与listPageCC相同）
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.apply(" a.goods=b.id and b.storage=c.id and b.goodsType=d.id ");
        if (StringUtils.isNotBlank(name) && !"null".equals(name)) {
            queryWrapper.like("b.name", name);
        }
        if (StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)) {
            queryWrapper.eq("d.id", Integer.parseInt(goodstype));
        }
        if (StringUtils.isNotBlank(storage) && !"null".equals(storage)) {
            queryWrapper.eq("c.id", Integer.parseInt(storage));
        }
        if (StringUtils.isNotBlank(operationType) && !"null".equals(operationType)) {
            queryWrapper.eq("a.operation_type", operationType);
        }
        if (StringUtils.isNotBlank(startDate) && !"null".equals(startDate)) {
            queryWrapper.ge("a.createtime", startDate);
        }
        if (StringUtils.isNotBlank(endDate) && !"null".equals(endDate)) {
            queryWrapper.le("a.createtime", endDate);
        }
        
        // 查询所有符合条件的记录（不分页）
        IPage<RecordRes> resultPage = recordMapper.pageCC  (new Page<>(1, Integer.MAX_VALUE), queryWrapper);
        List<RecordRes> recordList = resultPage.getRecords();
        
        // 转换为导出VO
        List<RecordExportVO> exportList = new ArrayList<>();
        for (RecordRes record : recordList) {
            RecordExportVO vo = new RecordExportVO();
            vo.setId(record.getId());
            vo.setGoodsName(record.getGoodsname());
            vo.setStorageName(record.getStoragename());
            vo.setGoodsTypeName(record.getGoodstypename());
            vo.setOperationType(record.getOperationType());
            vo.setRefOrderNum(record.getRefOrderNum());
            vo.setCount(record.getCount());
            vo.setUsername(record.getUsername());
            vo.setAdminname(record.getAdminname());
            // 格式化时间为字符串
            if (record.getCreatetime() != null) {
                vo.setCreatetime(record.getCreatetime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                vo.setCreatetime("");
            }
            vo.setRemark(record.getRemark());
            // 状态转换
            if (record.getStatus() != null) {
                switch (record.getStatus()) {
                    case 0:
                        vo.setStatusDesc("待审核");
                        break;
                    case 1:
                        vo.setStatusDesc("已完成");
                        break;
                    case 2:
                        vo.setStatusDesc("已拒绝");
                        break;
                    default:
                        vo.setStatusDesc("未知");
                }
            }
            exportList.add(vo);
        }
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("出入库记录_" + System.currentTimeMillis(), "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        
        // 使用EasyExcel导出，设置自动列宽
        EasyExcel.write(response.getOutputStream(), RecordExportVO.class)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("出入库记录")
                .doWrite(exportList);
    }
}
