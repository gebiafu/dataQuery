package com.example.dataquery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 查询事件请求DTO
 */
@Data
public class QueryEventRequest {
    
    private Long id;
    
    @NotBlank(message = "事件名称不能为空")
    private String name;
    
    @NotBlank(message = "事件类型不能为空")
    private String eventType;  // query, insert, update, delete
    
    @NotNull(message = "数据源不能为空")
    private Long dataSourceId;
    
    @NotBlank(message = "SQL内容不能为空")
    private String sqlContent;
    
    private String description;
}
