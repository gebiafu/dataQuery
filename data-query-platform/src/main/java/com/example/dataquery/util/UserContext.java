package com.example.dataquery.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 */
public class UserContext {
    
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }
    
    public static Long getUserId() {
        return userIdHolder.get();
    }
    
    public static void setUsername(String username) {
        usernameHolder.set(username);
    }
    
    public static String getUsername() {
        return usernameHolder.get();
    }
    
    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
    }
    
    /**
     * 获取当前请求
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    /**
     * 获取Token
     */
    public static String getToken() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                return token.substring(7);
            }
        }
        return null;
    }
}
