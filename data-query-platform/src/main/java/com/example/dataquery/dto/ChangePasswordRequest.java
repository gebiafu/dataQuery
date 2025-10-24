package com.example.dataquery.dto;

import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class ChangePasswordRequest {
    
    private Long userId;
    
    private String oldPassword;
    
    private String newPassword;
}