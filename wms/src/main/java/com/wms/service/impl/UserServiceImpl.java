package com.wms.service.impl;

import com.wms.entity.User;
import com.wms.mapper.UserMapper;
import com.wms.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>
 * 用户服务实现类
 * </p>
 *
 * @author wms
 * @since 2026-03-08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public boolean updatePassword(Integer userId, String oldPassword, String newPassword) {
        // 根据userId查询用户
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 对原密码进行MD5加密后比对
        String encryptedOldPassword = MD5(oldPassword);
        if (!encryptedOldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 对新密码进行MD5加密后存储
        String encryptedNewPassword = MD5(newPassword);
        user.setPassword(encryptedNewPassword);
        return this.updateById(user);
    }

    @Override
    public boolean cancelAccount(Integer userId) {
        // 根据userId查询用户
        User user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 将 isValid 设置为 'N' 表示账号已注销/停用
        user.setIsValid("N");
        return this.updateById(user);
    }

    /**
     * MD5加密方法
     * @param input 待加密的字符串
     * @return 加密后的32位十六进制字符串
     */
    private String MD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

}
