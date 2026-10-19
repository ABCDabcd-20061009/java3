package com.fenglin.springboottest.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * JwtUtil 单元测试：签发/解析、伪造与过期 token 的拒绝
 */
class JwtUtilTest {

    private static final String SECRET = "unit-test-secret-2026";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 7);

    @Test
    @DisplayName("签发的 token 能被正确解析出用户ID与用户名")
    void generateAndParse() {
        String token = jwtUtil.generate(42L, "梦梦");

        assertEquals(42L, jwtUtil.getUserId(token));
        assertEquals("梦梦", jwtUtil.getUsername(token));
        assertEquals(7, jwtUtil.getExpireDays());
    }

    @Test
    @DisplayName("空值 / 乱码 token 返回 null，不抛异常")
    void invalidTokenReturnsNull() {
        assertNull(jwtUtil.parse(null));
        assertNull(jwtUtil.parse(""));
        assertNull(jwtUtil.parse("   "));
        assertNull(jwtUtil.parse("not-a-jwt"));
        assertNull(jwtUtil.getUserId("not-a-jwt"));
    }

    @Test
    @DisplayName("用别的密钥签发的 token 校验不通过（防伪造）")
    void tokenSignedByOtherSecretIsRejected() {
        JwtUtil other = new JwtUtil("another-secret-key-2026", 7);
        String foreignToken = other.generate(1L, "星星");

        assertNull(jwtUtil.getUserId(foreignToken));
    }

    @Test
    @DisplayName("已过期的 token 校验不通过")
    void expiredTokenIsRejected() throws Exception {
        byte[] key = MessageDigest.getInstance("SHA-256").digest(SECRET.getBytes(StandardCharsets.UTF_8));
        String expired = Jwts.builder()
                .subject("7")
                .claim("username", "夜风")
                .issuedAt(new Date(System.currentTimeMillis() - 200_000L))
                .expiration(new Date(System.currentTimeMillis() - 100_000L))
                .signWith(Keys.hmacShaKeyFor(key))
                .compact();

        assertNull(jwtUtil.parse(expired));
        assertNull(jwtUtil.getUserId(expired));
    }

    @Test
    @DisplayName("被篡改内容的 token 校验不通过")
    void tamperedTokenIsRejected() {
        String token = jwtUtil.generate(1L, "梦梦");
        // 改动签名段，破坏签名
        String tampered = token.substring(0, token.length() - 3) + "abc";

        assertNull(jwtUtil.getUserId(tampered));
    }
}
