package com.example.dataquery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 数据源请求DTO
 */
@Data
public class DataSourceRequest {
    
    private Long id;
    
    @NotBlank(message = "数据源名称不能为空")
    private String name;
    
    @NotBlank(message = "数据源类型不能为空")
    private String type;  // mysql, postgresql
    
    @NotBlank(message = "主机地址不能为空")
    private String host;
    
    @NotNull(message = "端口不能为空")
    private Integer port;
    
    @NotBlank(message = "数据库名不能为空")
    private String databaseName;
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String description;
}
