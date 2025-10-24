package com.example.dataquery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度任务响应DTO
 */
@Data
public class ScheduleJobResponse {

    private Long id;
    private String jobName;
    private Long eventId;
    private String eventName;
    private String cronExpression;
    private String jobStatus;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
