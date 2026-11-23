package com.wms.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT拦截器
 * 拦截请求并验证JWT Token
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /**
     * 不需要拦截的路径（白名单）
     */
    private static final String[] WHITE_LIST = {
            "/user/login",
            "/user/jwtLogin",
            "/user/findByNo",
            "/error"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求路径
        String requestURI = request.getRequestURI();
        log.info("JWT拦截器 - 请求路径：{}", requestURI);

        // 检查是否在白名单中
        for (String whitePath : WHITE_LIST) {
            if (requestURI.contains(whitePath)) {
                log.info("JWT拦截器 - 白名单路径，放行：{}", whitePath);
                return true;
            }
        }

        // 跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头中获取Token
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            // 尝试从参数中获取
            token = request.getParameter("token");
        }

        // 移除Bearer前缀（如果有）
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证Token
        if (token == null || token.isEmpty()) {
            log.warn("JWT拦截器 - Token为空，拒绝访问");
            writeErrorResponse(response, 401, "未登录或Token已过期，请重新登录");
            return false;
        }

        // 验证Token是否有效
        if (!JwtUtils.isTokenValid(token)) {
            log.warn("JWT拦截器 - Token无效或已过期，拒绝访问");
            writeErrorResponse(response, 401, "Token已过期，请重新登录");
            return false;
        }

        // Token有效，将用户信息存入请求属性
        Integer userId = JwtUtils.getUserIdFromToken(token);
        String username = JwtUtils.getUsernameFromToken(token);
        Integer roleId = JwtUtils.getRoleIdFromToken(token);
        Boolean rememberMe = JwtUtils.getRememberMeFromToken(token);

        request.setAttribute("userId", userId);
        request.setAttribute("username", username);
        request.setAttribute("roleId", roleId);
        request.setAttribute("rememberMe", rememberMe);

        log.info("JWT拦截器 - Token验证通过，用户：{}，角色：{}，记住我：{}", username, roleId, rememberMe);

        // 角色权限校验 — 检查 @RequireRole 注解
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole == null) {
                requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
            }
            if (requireRole != null) {
                boolean allowed = false;
                for (int role : requireRole.value()) {
                    if (roleId != null && roleId == role) {
                        allowed = true;
                        break;
                    }
                }
                if (!allowed) {
                    log.warn("JWT拦截器 - 角色{}无权访问{}", roleId, requestURI);
                    writeErrorResponse(response, 403, "权限不足，当前角色无权访问此接口");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("msg", message);
        result.put("data", null);
        result.put("total", 0L);

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
