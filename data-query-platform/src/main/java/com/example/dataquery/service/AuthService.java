package com.example.dataquery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.dto.LoginRequest;
import com.example.dataquery.dto.LoginResponse;
import com.example.dataquery.dto.RegisterRequest;
import com.example.dataquery.entity.SysUser;
import com.example.dataquery.exception.BusinessException;
import com.example.dataquery.mapper.SysUserMapper;
import com.example.dataquery.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService extends ServiceImpl<SysUserMapper, SysUser> {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = this.getOne(queryWrapper);
        
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 验证密码
        String encodedPassword = encodePassword(request.getPassword());
        if (!encodedPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getRole());
    }
    
    /**
     * 用户注册
     */
    public void register(RegisterRequest request) {
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, request.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(encodePassword(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setRole("user");  // 默认普通用户
        
        this.save(user);
    }
    
    /**
     * 密码加密
     */
    private String encodePassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }
}
