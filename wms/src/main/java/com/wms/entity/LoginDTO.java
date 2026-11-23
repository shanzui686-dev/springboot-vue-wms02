package com.wms.entity;

import lombok.Data;

/**
 * 用户登录DTO
 * 用于接收前端登录请求参数
 */
@Data
public class LoginDTO {
    /**
     * 账号
     */
    private String no;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否记住我（7天免登录）
     * true: Token有效期7天
     * false: Token有效期12小时
     */
    private Boolean rememberMe = false;
}
