package com.example.dataquery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度日志响应DTO
 */
@Data
public class ScheduleLogResponse {

    private Long id;
    private Long jobId;
    private String jobName;
    private Long eventId;
    private String eventName;
    private String executeStatus;
    private String executeResult;
    private String errorMessage;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    private Long duration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
