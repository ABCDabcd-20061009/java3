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
 * JWT 工具：登录成功后签发 token，后续请求用 Authorization: Bearer &lt;token&gt; 携带。
 * 密钥与有效期通过配置项 dream.jwt.secret / dream.jwt.expire-days 指定。
 */
@Component
public class JwtUtil {

    /** 用户名放到自定义 claim 里，方便接口直接取用 */
    private static final String CLAIM_USERNAME = "username";

    private final SecretKey key;
    private final long expireMillis;

    public JwtUtil(@Value("${dream.jwt.secret:dream-login-dev-secret-change-me}") String secret,
                   @Value("${dream.jwt.expire-days:7}") long expireDays) {
        // 密钥长度对 HS256 有 256bit 要求，统一做一次 SHA-256 派生，避免配置太短直接抛异常
        this.key = Keys.hmacShaKeyFor(sha256(secret));
        this.expireMillis = Math.max(expireDays, 1) * 24L * 60 * 60 * 1000;
    }

    /** 签发 token */
    public String generate(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_USERNAME, username)
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
