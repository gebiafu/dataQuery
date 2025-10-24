package com.example.dataquery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * SQL执行请求DTO
 */
@Data
public class SqlExecuteRequest {
    
    @NotNull(message = "数据源不能为空")
    private Long dataSourceId;
    
    @NotBlank(message = "SQL不能为空")
    private String sql;
    
    private Long eventId;
}
