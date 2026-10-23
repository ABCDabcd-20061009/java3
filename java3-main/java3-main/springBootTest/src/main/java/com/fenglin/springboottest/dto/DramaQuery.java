package com.fenglin.springboottest.dto;

/**
 * 广播剧列表查询条件（前端 POST JSON）
 */
public class DramaQuery {

    /** 页码，从 1 开始，默认 1 */
    private Integer page = 1;

    /** 每页条数，默认 8 */
    private Integer size = 8;

    /** 关键词：模糊匹配 标题/简介/标签 */
    private String keyword;

    /** 分类筛选：不传或 "全部" 表示不限；可选 悬疑/言情/科幻/治愈/古风/儿童 */
    private String category;

    /** 来源平台筛选：不传或 "全部" 表示不限；可选 喜马拉雅/猫耳FM/晋江文学城/蜻蜓FM 等 */
    private String platform;

    /** 排序：newest(默认，按时间倒序) / hot(按浏览量倒序) */
    private String sort = "newest";

    /** 当前登录用户ID：预留，用于未来的「是否已收藏」等状态 */
    private Long userId;

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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
