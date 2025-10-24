package com.example.dataquery.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.dataquery.common.PageRequest;
import com.example.dataquery.common.Result;
import com.example.dataquery.dto.ChangePasswordRequest;
import com.example.dataquery.dto.UserManagementRequest;
import com.example.dataquery.dto.UserManagementResponse;
import com.example.dataquery.service.UserManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserManagementController {
    
    @Autowired
    private UserManagementService userManagementService;
    
    /**
     * 分页查询用户
     */
    @GetMapping("/page")
    public Result<IPage<UserManagementResponse>> page(
            PageRequest pageRequest,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role) {
        IPage<UserManagementResponse> page = userManagementService.pageUsers(pageRequest, username, role);
        return Result.success(page);
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public Result<Void> create(@RequestBody UserManagementRequest request) {
        userManagementService.createUser(request);
        return Result.success();
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserManagementRequest request) {
        userManagementService.updateUser(id, request);
        return Result.success();
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return Result.success();
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserManagementResponse> getById(@PathVariable Long id) {
        UserManagementResponse user = userManagementService.getUserById(id);
        return Result.success(user);
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody String newPassword) {
        userManagementService.resetPassword(id, newPassword);
        return Result.success();
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        userManagementService.changePassword(request);
        return Result.success();
    }
}
