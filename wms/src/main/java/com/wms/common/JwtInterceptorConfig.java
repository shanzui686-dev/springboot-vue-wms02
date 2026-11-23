package com.wms.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * JWT拦截器配置类
 * 注册JWT拦截器到Spring MVC
 */
@Configuration
public class JwtInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(
                        "/user/login",           // 排除登录接口
                        "/user/jwtLogin",        // 排除JWT登录接口
                        "/user/findByNo",        // 排除根据编号查询接口
                        "/error",                // 排除错误页面
                        "/swagger-resources/**", // 排除Swagger相关
                        "/v3/api-docs/**",       // 排除Swagger文档
                        "/webjars/**",           // 排除静态资源
                        "/swagger-ui.html"       // 排除Swagger UI
                );
    }
}
