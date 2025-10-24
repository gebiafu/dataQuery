package com.example.dataquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询事件实体
 */
@Data
@TableName("query_event")
public class QueryEvent implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String eventType;  // query, insert, update, delete
    
    private Long dataSourceId;
    
    private String sqlContent;
    
    private String description;
    
    private Integer status;
    
    private Long createUserId;
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
