package com.fenglin.springboottest.vo;

/**
 * 后台数据概览统计（8 项）
 */
public class AdminStatsVO {

    /** 梦境总数（含草稿） */
    private long dreamCount;
    /** 已发布梦境数 */
    private long dreamPublishedCount;
    /** 广播剧总数 */
    private long dramaCount;
    /** 注册用户总数 */
    private long userCount;
    /** 累计浏览量（梦境） */
    private long viewCount;
    /** 累计点赞数（梦境） */
    private long likeCount;
    /** 累计收藏数 */
    private long favoriteCount;
    /** 搜索次数 */
    private long searchCount;

    public long getDreamCount() {
        return dreamCount;
    }

    public void setDreamCount(long dreamCount) {
        this.dreamCount = dreamCount;
    }

    public long getDreamPublishedCount() {
        return dreamPublishedCount;
    }

    public void setDreamPublishedCount(long dreamPublishedCount) {
        this.dreamPublishedCount = dreamPublishedCount;
    }

    public long getDramaCount() {
        return dramaCount;
    }

    public void setDramaCount(long dramaCount) {
        this.dramaCount = dramaCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(long likeCount) {
        this.likeCount = likeCount;
    }

    public long getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(long favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public long getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(long searchCount) {
        this.searchCount = searchCount;
    }
}
