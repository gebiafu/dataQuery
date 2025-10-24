package com.example.dataquery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户管理响应
 */
@Data
public class UserManagementResponse {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String phone;

    private String role;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
