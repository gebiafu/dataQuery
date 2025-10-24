package com.example.dataquery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * SQL执行响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlExecuteResponse {
    
    private Boolean success;
    
    private String message;
    
    private Integer affectedRows;  // 影响行数（增删改）
    
    private List<String> columns;  // 列名（查询）
    
    private List<Map<String, Object>> data;  // 数据（查询）
    
    private Long duration;  // 执行时长（毫秒）
}
