package com.fenglin.springboottest.service;

import com.fenglin.springboottest.entity.User;

public interface UserService {

    /**
     * 为指定用户名生成注册验证码。
     * 验证码会打印到后端控制台，并作为返回值返回（仅便于开发联调）。
     *
     * @return 演示用验证码
     */
    String sendRegisterCode(String username);

    /**
     * 注册新用户
     */
    User register(String username, String password, String code);

    /**
     * 登录：account 为用户名
     */
    User login(String account, String password);

    /**
     * 按主键查用户（/api/user-info 用），不存在返回 null
     */
    User getById(Long id);
}
