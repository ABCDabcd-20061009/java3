package com.fenglin.springboottest.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码内存存储（带过期时间）。
 * 课程项目够用；生产环境建议换成 Redis。
 */
@Component
public class CodeStore {

    /** 验证码有效期：5 分钟 */
    private static final long EXPIRE_MILLIS = 5 * 60 * 1000L;

    private final SecureRandom random = new SecureRandom();

    private static class Entry {
        final String code;
        final long expireAt;

        Entry(String code, long expireAt) {
            this.code = code;
            this.expireAt = expireAt;
        }
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /** 生成并保存 6 位数字验证码 */
    public String generate(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        store.put(email, new Entry(code, System.currentTimeMillis() + EXPIRE_MILLIS));
        return code;
    }

    /** 校验验证码，成功即失效（一次性） */
    public boolean verify(String email, String code) {
        Entry entry = store.get(email);
        if (entry == null) {
            return false;
        }
        if (System.currentTimeMillis() > entry.expireAt) {
            store.remove(email);
            return false;
        }
        if (!entry.code.equals(code)) {
            return false;
        }
        store.remove(email);
        return true;
    }
}
