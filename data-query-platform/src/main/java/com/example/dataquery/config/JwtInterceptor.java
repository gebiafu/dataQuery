package com.example.dataquery.config;

import com.example.dataquery.util.JwtUtil;
import com.example.dataquery.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 放行OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 获取Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            
            try {
                // 验证Token
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    // 设置用户上下文
                    UserContext.setUserId(userId);
                    UserContext.setUsername(username);
                    
                    return true;
                }
            } catch (Exception e) {
                log.error("Token验证失败：", e);
            }
        }
        
        // Token无效
        response.setStatus(401);
        return false;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 清除用户上下文
        UserContext.clear();
    }
}
