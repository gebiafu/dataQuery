package com.example.dataquery.dto;

import lombok.Data;

/**
 * 用户管理请求
 */
@Data
public class UserManagementRequest {
    
    private String username;
    
    private String password;
    
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String role;
    
    private Integer status;
}
