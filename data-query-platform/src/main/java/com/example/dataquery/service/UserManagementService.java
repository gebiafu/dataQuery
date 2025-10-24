package com.example.dataquery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.dto.ChangePasswordRequest;
import com.example.dataquery.dto.UserManagementRequest;
import com.example.dataquery.dto.UserManagementResponse;
import com.example.dataquery.entity.SysUser;
import com.example.dataquery.exception.BusinessException;
import com.example.dataquery.mapper.SysUserMapper;
import com.example.dataquery.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

/**
 * 用户管理服务
 */
@Slf4j
@Service
public class UserManagementService extends ServiceImpl<SysUserMapper, SysUser> {
    
    /**
     * 分页查询用户
     */
    public IPage<UserManagementResponse> pageUsers(PageRequest pageRequest, String username, String role) {
        Page<SysUser> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            queryWrapper.like(SysUser::getUsername, username);
        }
        if (StringUtils.hasText(role)) {
            queryWrapper.eq(SysUser::getRole, role);
        }
        queryWrapper.orderByDesc(SysUser::getCreateTime);
        
        IPage<SysUser> userPage = this.page(page, queryWrapper);
        
        return userPage.convert(this::convertToResponse);
    }
    
    /**
     * 创建用户
     */
    public void createUser(UserManagementRequest request) {
        // 验证权限
        checkAdminRole();
        
        // 检查用户名是否存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, request.getUsername());
        if (this.count(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        SysUser user = new SysUser();
        BeanUtils.copyProperties(request, user);
        user.setPassword(encodePassword(request.getPassword()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setRole(request.getRole() != null ? request.getRole() : "user");
        
        this.save(user);
    }
    
    /**
     * 更新用户
     */
    public void updateUser(Long id, UserManagementRequest request) {
        // 验证权限
        checkAdminRole();
        
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果修改用户名，检查是否重复
        if (StringUtils.hasText(request.getUsername()) && 
            !request.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUsername, request.getUsername());
            if (this.count(queryWrapper) > 0) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        
        this.updateById(user);
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        // 验证权限
        checkAdminRole();
        
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不能删除自己
        if (id.equals(UserContext.getUserId())) {
            throw new BusinessException("不能删除当前登录用户");
        }
        
        this.removeById(id);
    }
    
    /**
     * 重置密码
     */
    public void resetPassword(Long id, String newPassword) {
        // 验证权限
        checkAdminRole();
        
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        user.setPassword(encodePassword(newPassword));
        this.updateById(user);
    }
    
    /**
     * 修改密码
     */
    public void changePassword(ChangePasswordRequest request) {
        Long userId = request.getUserId();
        if (userId == null) {
            userId = UserContext.getUserId();
        }
        
        // 只能修改自己的密码，除非是管理员
        if (!userId.equals(UserContext.getUserId())) {
            checkAdminRole();
        }
        
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 验证旧密码
        if (StringUtils.hasText(request.getOldPassword())) {
            String encodedOldPassword = encodePassword(request.getOldPassword());
            if (!encodedOldPassword.equals(user.getPassword())) {
                throw new BusinessException("旧密码错误");
            }
        }
        
        user.setPassword(encodePassword(request.getNewPassword()));
        this.updateById(user);
    }
    
    /**
     * 获取用户详情
     */
    public UserManagementResponse getUserById(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToResponse(user);
    }
    
    /**
     * 检查是否是管理员
     */
    private void checkAdminRole() {
        Long userId = UserContext.getUserId();
        SysUser currentUser = this.getById(userId);
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            throw new BusinessException("无权限操作");
        }
    }
    
    /**
     * 转换为响应对象
     */
    private UserManagementResponse convertToResponse(SysUser user) {
        UserManagementResponse response = new UserManagementResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }
    
    /**
     * 密码加密
     */
    private String encodePassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }
}
