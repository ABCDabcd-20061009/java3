package com.fenglin.springboottest.dto;

/**
 * 编辑梦境请求体：{ id, title, summary, content, coverUrl, category, tags }
 * 只能编辑自己发布的梦境，作者身份由 token 决定。
 */
public class DreamUpdateDTO {

    /** 要编辑的梦境ID */
    private Long id;

    /** 标题（必填，2-100 字） */
    private String title;

    /** 摘要（选填，<=255，空则取正文前 60 字） */
    private String summary;

    /** 正文（必填） */
    private String content;

    /** 封面图URL（选填） */
    private String coverUrl;

    /** 分类（选填，非法值回落「随笔」） */
    private String category;

    /** 标签，逗号分隔（选填，最多 8 个） */
    private String tags;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
