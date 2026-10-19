package com.fenglin.springboottest.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码工具：SHA-256 加盐哈希
 */
public class PasswordUtil {

    /** 固定盐，正式环境建议每个用户独立随机盐 */
    private static final String SALT = "dream-login@2026";

    private PasswordUtil() {
    }

    public static String encode(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((SALT + rawPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

    public static boolean matches(String rawPassword, String encoded) {
        return encode(rawPassword).equals(encoded);
    }
}
