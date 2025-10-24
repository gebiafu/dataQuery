package com.example.dataquery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.common.Result;
import com.example.dataquery.dto.*;
import com.example.dataquery.service.QueryEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 查询事件控制器
 */
@RestController
@RequestMapping("/api/event")
public class QueryEventController {

    @Autowired
    private QueryEventService queryEventService;

    /**
     * 创建事件
     */
    @PostMapping
    public Result<String> create(@Validated @RequestBody QueryEventRequest request) {
        queryEventService.createEvent(request);
        return Result.success("创建成功");
    }

    /**
     * 更新事件
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Validated @RequestBody QueryEventRequest request) {
        request.setId(id);
        queryEventService.updateEvent(request);
        return Result.success("更新成功");
    }

    /**
     * 删除事件
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        queryEventService.deleteEvent(id);
        return Result.success("删除成功");
    }

    /**
     * 分页查询事件列表
     */
    @GetMapping("/page")
    public Result<IPage<QueryEventResponse>> page(PageRequest pageRequest,
                                                    @RequestParam(required = false) String name,
                                                    @RequestParam(required = false) String eventType,
                                                    @RequestParam(required = false) Long dataSourceId) {
        IPage<QueryEventResponse> page = queryEventService.listEvents(pageRequest, name, eventType, dataSourceId);
        return Result.success(page);
    }

    /**
     * 查询所有事件
     */
    @GetMapping("/list")
    public Result<List<QueryEventResponse>> list() {
        List<QueryEventResponse> list = queryEventService.listAllEvents();
        return Result.success(list);
    }

    /**
     * 获取事件详情
     */
    @GetMapping("/{id}")
    public Result<QueryEventResponse> getById(@PathVariable Long id) {
        QueryEventResponse response = queryEventService.getEventById(id);
        return Result.success(response);
    }

    /**
     * 执行SQL
     */
    @PostMapping("/execute")
    public Result<SqlExecuteResponse> execute(@Validated @RequestBody SqlExecuteRequest request) {
        SqlExecuteResponse response = queryEventService.executeSql(request);
        return Result.success(response);
    }
}
