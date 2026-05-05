package com.wms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.Result;
import com.wms.entity.SysLog;
import com.wms.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统操作日志Controller
 */
@RestController
@RequestMapping("/log")
public class SysLogController {

    @Autowired
    private ISysLogService sysLogService;

    /**
     * 分页查询操作日志
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param username 操作人（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页结果
     */
    @GetMapping("/listPage")
    public Result listPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        
        Page<SysLog> page = new Page<>(pageNum, pageSize);
        IPage<SysLog> result = sysLogService.pageList(page, username, startTime, endTime);
        
        return Result.suc(result.getRecords(), result.getTotal());
    }
}