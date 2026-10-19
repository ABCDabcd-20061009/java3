package com.fenglin.springboottest.dto;

/**
 * 互动请求体（点赞 / 收藏共用）：{ id, userId, action: like | unlike | toggle }
 * 注意：userId 仅作兼容保留，实际身份以 JWT 解析出的登录用户为准。
 */
public class DreamActionDTO {

    /** 梦境ID */
    private Long id;

    /** 操作用户ID（前端会话带入，服务端以 token 身份为准） */
    private Long userId;

    /** 操作类型：toggle 切换（默认，可不传）/ 正向动作 / 反向动作，取值见各接口注释 */
    private String action = "toggle";

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
