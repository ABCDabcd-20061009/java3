package com.fenglin.springboottest.common;

/**
 * 业务异常：message 会直接返回给前端展示
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
