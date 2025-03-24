package com.example.demo.util;

import com.example.demo.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * 安全工具类
 * 用于处理认证信息，在开发阶段提供默认用户
 */
public class SecurityUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    
    // 默认测试用户ID
    private static final Long DEFAULT_USER_ID = 1L;
    
    /**
     * 获取当前用户，如果认证为空则返回测试用户
     *
     * @param authentication Spring Security认证对象
     * @return 用户对象，永不为null
     */
    public static User getCurrentUserOrTestUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        
        logger.debug("认证对象为空或不包含用户信息，使用测试用户");
        
        // 创建测试用户
        User testUser = new User();
        testUser.setId(DEFAULT_USER_ID);
        testUser.setUsername("test_user");
        testUser.setEmail("test@example.com");
        
        return testUser;
    }
    
    /**
     * 获取当前用户ID，如果认证为空则返回默认用户ID
     *
     * @param authentication Spring Security认证对象
     * @return 用户ID，永不为null
     */
    public static Long getCurrentUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        
        logger.debug("认证对象为空或不包含用户信息，使用默认用户ID: {}", DEFAULT_USER_ID);
        return DEFAULT_USER_ID;
    }
    
    /**
     * 检查当前用户是否已认证
     *
     * @param authentication Spring Security认证对象
     * @return 是否已认证
     */
    public static boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() 
               && authentication.getPrincipal() instanceof User;
    }
} 