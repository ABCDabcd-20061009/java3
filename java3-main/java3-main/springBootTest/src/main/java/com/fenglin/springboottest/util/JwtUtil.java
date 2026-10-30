package com.fenglin.springboottest.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * JWT 工具：登录成功后签发 token，后续请求用 Authorization: Bearer <token> 携带。
 * 密钥与有效期通过配置项 dream.jwt.secret / dream.jwt.expire-days 指定。
 *
 * token 中携带两个业务 claim：
 * - username：用户名，方便接口直接取用
 * - role：用户角色（user / admin），后台管理系统据此区分管理员
 */
@Component
public class JwtUtil {

    /** 用户名放到自定义 claim 里，方便接口直接取用 */
    private static final String CLAIM_USERNAME = "username";

    /** 角色 claim：user=普通用户，admin=后台管理员 */
    private static final String CLAIM_ROLE = "role";

    /** 普通用户角色 */
    public static final String ROLE_USER = "user";

    /** 管理员角色 */
    public static final String ROLE_ADMIN = "admin";

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${dream.jwt.secret:dream-login-dev-secret-change-me}") String secret,
                   @Value("${dream.jwt.expire-days:7}") long expireDays) {
        // 密钥长度对 HS256 有 256bit 要求，统一做一次 SHA-256 派生，避免配置太短直接抛异常
        this.key = Keys.hmacShaKeyFor(sha256(secret));
        this.expireMillis = Math.max(expireDays, 1) * 24L * 60 * 60 * 1000;
    }

    /** 签发普通用户 token（默认角色 user） */
    public String generate(Long userId, String username) {
        return generate(userId, username, ROLE_USER);
    }

    /** 签发带角色的 token（后台管理员登录使用 ROLE_ADMIN） */
    public String generate(Long userId, String username, String role) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role == null ? ROLE_USER : role)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireMillis))
                .signWith(key)
                .compact();
    }

    /** 解析并校验签名与有效期；非法或过期返回 null（不抛异常，交给调用方决定放行还是拒绝） */
    public Claims parse(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token.trim()).getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserId(String token) {
        Claims claims = parse(token);
        if (claims == null) {
            return null;
        }
        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getUsername(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.get(CLAIM_USERNAME, String.class);
    }

    /** 取 token 角色；非管理员返回 null */
    public String getRole(String token) {
        Claims claims = parse(token);
        return claims == null ? null : claims.get(CLAIM_ROLE, String.class);
    }

    /** token 有效期（天），配置回显用 */
    public long getExpireDays() {
        return expireMillis / (24L * 60 * 60 * 1000);
    }

    private static byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }
}
