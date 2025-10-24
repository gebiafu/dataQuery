package com.example.dataquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 调度任务实体
 */
@Data
@TableName("schedule_job")
public class ScheduleJob implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String jobName;
    
    private Long eventId;
    
    private String cronExpression;
    
    private String jobStatus;  // RUNNING, STOP
    
    private String description;
    
    private Long createUserId;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
