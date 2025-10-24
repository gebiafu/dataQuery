package com.example.dataquery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据源实体
 */
@Data
@TableName("data_source")
public class DataSource implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String type;  // mysql, postgresql
    
    private String host;
    
    private Integer port;
    
    private String databaseName;
    
    private String username;
    
    private String password;
    
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
