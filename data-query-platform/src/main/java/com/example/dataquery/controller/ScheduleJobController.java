package com.example.dataquery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.common.Result;
import com.example.dataquery.dto.ScheduleJobRequest;
import com.example.dataquery.dto.ScheduleJobResponse;
import com.example.dataquery.service.ScheduleJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 调度任务控制器
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleJobController {
    
    @Autowired
    private ScheduleJobService scheduleJobService;
    
    /**
     * 创建调度任务
     */
    @PostMapping
    public Result<String> create(@Validated @RequestBody ScheduleJobRequest request) {
        scheduleJobService.createJob(request);
        return Result.success("创建成功", null);
    }
    
    /**
     * 更新调度任务
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @Validated @RequestBody ScheduleJobRequest request) {
        request.setId(id);
        scheduleJobService.updateJob(request);
        return Result.success("更新成功", null);
    }
    
    /**
     * 删除调度任务
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        scheduleJobService.deleteJob(id);
        return Result.success("删除成功", null);
    }
    
    /**
     * 分页查询调度任务列表
     */
    @GetMapping("/page")
    public Result<IPage<ScheduleJobResponse>> page(PageRequest pageRequest,
                                                     @RequestParam(required = false) String jobName,
                                                     @RequestParam(required = false) String jobStatus) {
        IPage<ScheduleJobResponse> page = scheduleJobService.listJobs(pageRequest, jobName, jobStatus);
        return Result.success(page);
    }
    
    /**
     * 获取调度任务详情
     */
    @GetMapping("/{id}")
    public Result<ScheduleJobResponse> getById(@PathVariable Long id) {
        ScheduleJobResponse response = scheduleJobService.getJobById(id);
        return Result.success(response);
    }
    
    /**
     * 启动任务
     */
    @PostMapping("/{id}/start")
    public Result<String> start(@PathVariable Long id) {
        scheduleJobService.startJob(id);
        return Result.success("启动成功", null);
    }
    
    /**
     * 停止任务
     */
    @PostMapping("/{id}/stop")
    public Result<String> stop(@PathVariable Long id) {
        scheduleJobService.stopJob(id);
        return Result.success("停止成功", null);
    }
}
