package com.fenglin.springboottest.common;

/**
 * 鉴权异常：未登录或 token 失效时抛出，由全局异常处理返回 HTTP 401。
 * 前端 api.js 收到 401 会清理会话并回到登录页。
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
