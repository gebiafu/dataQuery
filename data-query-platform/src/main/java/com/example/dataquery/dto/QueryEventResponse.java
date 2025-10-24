package com.example.dataquery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查询事件响应DTO
 */
@Data
public class QueryEventResponse {

    private Long id;
    private String name;
    private String eventType;
    private Long dataSourceId;
    private String dataSourceName;
    private String sqlContent;
    private String description;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
