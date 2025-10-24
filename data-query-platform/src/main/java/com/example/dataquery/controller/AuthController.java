package com.example.dataquery.controller;

import com.example.dataquery.common.Result;
import com.example.dataquery.dto.LoginRequest;
import com.example.dataquery.dto.LoginResponse;
import com.example.dataquery.dto.RegisterRequest;
import com.example.dataquery.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success("注册成功");
    }
}
