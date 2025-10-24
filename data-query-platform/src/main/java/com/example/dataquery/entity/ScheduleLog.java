package com.example.dataquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调度日志实体
 */
@Data
@TableName("schedule_log")
public class ScheduleLog implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long jobId;
    
    private String jobName;
    
    private Long eventId;
    
    private String eventName;
    
    private String executeStatus;  // SUCCESS, FAIL
    
    private String executeResult;
    
    private String errorMessage;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Long duration;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
