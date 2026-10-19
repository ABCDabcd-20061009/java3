package com.fenglin.springboottest.dto;

/**
 * 首页列表查询条件（前端 POST JSON）
 */
public class DreamQuery {

    /** 页码，从 1 开始，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 10 */
    private Integer size = 10;

    /** 关键词：模糊匹配 标题/摘要/正文/标签 */
    private String keyword;

    /** 分类筛选：不传或 "全部" 表示不限；可选 美梦/噩梦/灵感/随笔 */
    private String category;

    /** 排序：newest(默认，按时间倒序) / hot(按浏览量倒序) */
    private String sort = "newest";

    /** 当前登录用户ID：用于返回每条内容「我是否已点赞」，未登录可不传 */
    private Long userId;

    /** 作者ID筛选：仅查某人发布的内容（「我的梦境」用，服务端会强制覆盖为登录用户） */
    private Long authorId;

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
