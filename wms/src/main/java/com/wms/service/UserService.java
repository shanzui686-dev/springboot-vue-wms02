package com.wms.service;

import com.wms.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户服务类
 * </p>
 *
 * @author wms
 * @since 2026-03-08
 */
public interface UserService extends IService<User> {

    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    boolean updatePassword(Integer userId, String oldPassword, String newPassword);

    /**
     * 注销账号（停用账号）
     * @param userId 用户ID
     * @return 注销结果
     */
    boolean cancelAccount(Integer userId);

}
