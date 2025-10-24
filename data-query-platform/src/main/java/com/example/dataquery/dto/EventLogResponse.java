package com.example.dataquery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件日志响应DTO
 */
@Data
public class EventLogResponse {

    private Long id;
    private Long eventId;
    private String eventName;
    private String eventType;
    private Long dataSourceId;
    private String dataSourceName;
    private String sqlContent;
    private String executeStatus;
    private String executeResult;
    private String errorMessage;
    private Long executeUserId;
    private String executeUserName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    private Long duration;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
