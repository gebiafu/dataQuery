package com.example.dataquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 事件日志实体
 */
@Data
@TableName("event_log")
public class EventLog implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long eventId;
    
    private String eventName;
    
    private String eventType;
    
    private Long dataSourceId;
    
    private String dataSourceName;
    
    private String sqlContent;
    
    private String executeStatus;  // SUCCESS, FAIL
    
    private String executeResult;
    
    private String errorMessage;
    
    private Long executeUserId;
    
    private String executeUserName;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long duration;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
