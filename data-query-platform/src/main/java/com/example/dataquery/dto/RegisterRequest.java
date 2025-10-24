package com.example.dataquery.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 注册请求DTO
 */
@Data
public class RegisterRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private String nickname;
    
    private String email;
    
    private String phone;
}
