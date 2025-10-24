package com.example.dataquery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.common.Result;
import com.example.dataquery.dto.ScheduleLogResponse;
import com.example.dataquery.service.ScheduleLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 调度日志控制器
 */
@RestController
@RequestMapping("/api/schedule-log")
public class ScheduleLogController {
    
    @Autowired
    private ScheduleLogService scheduleLogService;
    
    /**
     * 分页查询调度日志
     */
    @GetMapping("/page")
    public Result<IPage<ScheduleLogResponse>> page(PageRequest pageRequest,
                                                     @RequestParam(required = false) Long jobId,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                     @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                                     @RequestParam(required = false) String executeStatus) {
        IPage<ScheduleLogResponse> page = scheduleLogService.listScheduleLogs(pageRequest, jobId, startTime, endTime, executeStatus);
        return Result.success(page);
    }
}
