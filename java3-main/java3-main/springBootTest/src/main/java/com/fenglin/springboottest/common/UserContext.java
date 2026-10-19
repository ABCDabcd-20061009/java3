package com.fenglin.springboottest.common;

/**
 * 当前请求的登录用户上下文（由 JwtInterceptor 写入，请求结束后清理）。
 * 这样业务层不用再从请求体里信任前端传来的 userId。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(Long userId, String username) {
        USER_ID.set(userId);
        USERNAME.set(username);
    }

    /** 未登录返回 null */
    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUsername() {
        return USERNAME.get();
    }

    public static boolean isLogin() {
        return USER_ID.get() != null;
    }

    public static void clear() {
        USER_ID.remove();
        USERNAME.remove();
    }
}
