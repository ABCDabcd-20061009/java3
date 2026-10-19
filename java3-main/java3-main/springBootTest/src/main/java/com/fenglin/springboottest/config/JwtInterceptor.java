package com.fenglin.springboottest.config;

import com.fenglin.springboottest.common.AuthException;
import com.fenglin.springboottest.common.UserContext;
import com.fenglin.springboottest.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * JWT 拦截器：
 * - 带 token 且合法 → 写入 UserContext（业务层据此拿到真实身份）
 * - 带 token 但非法/过期 → 需鉴权的接口返回 401，其余接口按未登录处理
 * - 未带 token → 需鉴权的接口返回 401，其余接口放行（首页浏览、搜索不需要登录）
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /** 必须登录才能访问的接口（其余接口匿名可访问） */
    private static final Set<String> AUTH_REQUIRED = Set.of(
            "/api/dream/like",
            "/api/dream/favorite",
            "/api/dream/publish",
            "/api/dream/update",
            "/api/dream/delete",
            "/api/user-info",
            "/api/user/favorites",
            "/api/user/dreams",
            "/api/user/profile"
    );

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        boolean needAuth = AUTH_REQUIRED.contains(path);

        String token = resolveToken(request);
        if (token == null) {
            if (needAuth) {
                throw new AuthException("请先登录");
            }
            return true;
        }

        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            if (needAuth) {
                throw new AuthException("登录已过期，请重新登录");
            }
            return true;   // 匿名接口：token 失效就当未登录，不影响浏览
        }
        try {
            UserContext.set(Long.valueOf(claims.getSubject()), claims.get("username", String.class));
        } catch (NumberFormatException e) {
            if (needAuth) {
                throw new AuthException("登录状态异常，请重新登录");
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程复用，必须清理，避免身份串号
        UserContext.clear();
    }

    /** 支持标准的 Authorization: Bearer xxx */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || header.trim().isEmpty()) {
            return null;
        }
        String value = header.trim();
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            value = value.substring(7).trim();
        }
        return value.isEmpty() ? null : value;
    }
}
