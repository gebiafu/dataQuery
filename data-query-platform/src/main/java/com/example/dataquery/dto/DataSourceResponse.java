package com.example.dataquery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据源响应DTO
 */
@Data
public class DataSourceResponse {

    private Long id;
    private String name;
    private String type;
    private String host;
    private Integer port;
    private String databaseName;
    private String username;
    private String description;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
