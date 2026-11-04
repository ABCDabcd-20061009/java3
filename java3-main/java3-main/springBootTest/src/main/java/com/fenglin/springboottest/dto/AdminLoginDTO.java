package com.fenglin.springboottest.dto;

/**
 * 管理员登录参数：{ username, password }
 */
public class AdminLoginDTO {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
