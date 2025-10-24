package com.example.dataquery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.common.Result;
import com.example.dataquery.dto.EventLogResponse;
import com.example.dataquery.service.EventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 事件日志控制器
 */
@RestController
@RequestMapping("/api/event-log")
public class EventLogController {
    
    @Autowired
    private EventLogService eventLogService;
    
    /**
     * 分页查询事件日志
     */
    @GetMapping("/page")
    public Result<IPage<EventLogResponse>> page(PageRequest pageRequest,
                                                  @RequestParam(required = false) String eventType,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                                  @RequestParam(required = false) String executeStatus) {
        IPage<EventLogResponse> page = eventLogService.listEventLogs(pageRequest, eventType, startTime, endTime, executeStatus);
        return Result.success(page);
    }
}
