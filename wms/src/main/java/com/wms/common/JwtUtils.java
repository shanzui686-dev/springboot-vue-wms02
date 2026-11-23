package com.wms.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT Token，支持"7天免登录"功能
 */
@Slf4j
public class JwtUtils {

    /**
     * JWT密钥（建议使用环境变量或配置文件管理）
     */
    private static final String SECRET_KEY = "wms-jwt-secret-key-2026-springboot-vue";

    /**
     * 普通登录Token过期时间：12小时
     */
    private static final long EXPIRATION_TIME_12H = 12 * 60 * 60 * 1000L;

    /**
     * 记住我Token过期时间：7天
     */
    private static final long EXPIRATION_TIME_7D = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成JWT Token
     *
     * @param userId    用户ID
     * @param username  用户名
     * @param roleId    角色ID
     * @param rememberMe 是否记住我（true=7天，false=12小时）
     * @return JWT Token字符串
     */
    public static String generateToken(Integer userId, String username, Integer roleId, boolean rememberMe) {
        // 设置过期时间
        long expirationTime = rememberMe ? EXPIRATION_TIME_7D : EXPIRATION_TIME_12H;
        Date expirationDate = new Date(System.currentTimeMillis() + expirationTime);

        // 构建Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleId", roleId);
        claims.put("rememberMe", rememberMe);

        // 生成Token
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        log.info("生成JWT Token成功，用户：{}，记住我：{}，过期时间：{}", username, rememberMe, expirationDate);
        return token;
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token字符串
     * @return Claims对象，包含用户信息
     */
    public static Claims parseToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        } catch (Exception e) {
            log.error("解析JWT Token失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID
     */
    public static Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Integer.class);
        }
        return null;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token字符串
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.getSubject();
        }
        return null;
    }

    /**
     * 从Token中获取角色ID
     *
     * @param token JWT Token字符串
     * @return 角色ID
     */
    public static Integer getRoleIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("roleId", Integer.class);
        }
        return null;
    }

    /**
     * 从Token中获取"记住我"标志
     *
     * @param token JWT Token字符串
     * @return 是否记住我
     */
    public static Boolean getRememberMeFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("rememberMe", Boolean.class);
        }
        return false;
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token字符串
     * @return 是否有效
     */
    public static boolean isTokenValid(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            // 检查是否过期
            Date expiration = claims.getExpiration();
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取签名密钥
     *
     * @return Key对象
     */
    private static Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
