package com.fenglin.springboottest.dto;

/**
 * 详情请求体：{ id, userId }，userId 用于返回「我是否已点赞」，未登录可不传
 */
public class DreamDetailDTO {

    private Long id;

    private Long userId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
