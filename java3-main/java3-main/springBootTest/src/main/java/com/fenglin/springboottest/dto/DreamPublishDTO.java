package com.fenglin.springboottest.dto;

/**
 * 发布梦境请求体（前端 POST JSON）
 */
public class DreamPublishDTO {

    /** 作者用户ID（仅用于兼容旧前端，后端一律以 token 身份为准） */
    private Long userId;

    /**
     * 作者昵称（已废弃：作者名一律取登录账号的用户名，防止伪造署名）。
     * 保留字段只为兼容旧调用方，传了也不会生效。
     */
    @Deprecated
    private String authorName;

    /** 标题（必填，2-100 字） */
    private String title;

    /** 摘要（选填，<=255） */
    private String summary;

    /** 正文（选填，允许空） */
    private String content;

    /** 封面图URL（选填） */
    private String coverUrl;

    /** 分类（选填，默认 随笔） */
    private String category;

    /** 标签，逗号分隔（选填） */
    private String tags;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
