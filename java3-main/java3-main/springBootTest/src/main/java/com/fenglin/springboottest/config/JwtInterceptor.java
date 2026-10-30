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
 *
 * 后台管理接口（/api/admin/**，登录接口除外）额外要求 token 角色为 admin，
 * 否则返回 401，避免普通用户的 token 越权访问管理后台。
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

    /** 后台管理接口统一前缀（除登录接口外，都要求管理员身份） */
    private static final String ADMIN_PREFIX = "/api/admin/";

    /** 管理员登录接口，允许匿名访问 */
    private static final String ADMIN_LOGIN = "/api/admin/login";

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
        boolean isAdminPath = path.startsWith(ADMIN_PREFIX);

        String token = resolveToken(request);
        if (token == null) {
            if (isAdminPath && !ADMIN_LOGIN.equals(path)) {
                throw new AuthException("请先以管理员身份登录");
            }
            if (needAuth) {
                throw new AuthException("请先登录");
            }
            return true;
        }

        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            if (isAdminPath && !ADMIN_LOGIN.equals(path)) {
                throw new AuthException("管理员登录已过期，请重新登录");
            }
            if (needAuth) {
                throw new AuthException("登录已过期，请重新登录");
            }
            return true;   // 匿名接口：token 失效就当未登录，不影响浏览
        }

        if (isAdminPath && !ADMIN_LOGIN.equals(path)) {
            // 后台管理接口：必须是管理员 token
            String role = claims.get("role", String.class);
            if (!JwtUtil.ROLE_ADMIN.equals(role)) {
                throw new AuthException("无管理员权限");
            }
        }

        try {
            UserContext.set(Long.valueOf(claims.getSubject()), claims.get("username", String.class));
        } catch (NumberFormatException e) {
            if (isAdminPath && !ADMIN_LOGIN.equals(path)) {
                throw new AuthException("管理员登录状态异常，请重新登录");
            }
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
