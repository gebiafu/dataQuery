package com.example.dataquery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 调度任务请求DTO
 */
@Data
public class ScheduleJobRequest {
    
    private Long id;
    
    @NotBlank(message = "任务名称不能为空")
    private String jobName;
    
    @NotNull(message = "事件不能为空")
    private Long eventId;
    
    @NotBlank(message = "Cron表达式不能为空")
    private String cronExpression;
    
    private String description;
}
