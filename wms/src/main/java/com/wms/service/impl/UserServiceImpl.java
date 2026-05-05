package com.wms.service.impl;

import com.wms.entity.User;
import com.wms.mapper.UserMapper;
import com.wms.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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
        
        // 校验原密码是否正确
        if (!oldPassword.equals(user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(newPassword);
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

}
