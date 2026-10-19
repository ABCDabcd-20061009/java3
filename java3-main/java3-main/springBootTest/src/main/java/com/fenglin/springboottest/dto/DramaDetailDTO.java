package com.fenglin.springboottest.dto;

/**
 * 广播剧详情查询条件
 */
public class DramaDetailDTO {

    /** 广播剧ID（必填） */
    private Long id;

    /** 当前登录用户ID：预留，用于未来的互动状态 */
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
