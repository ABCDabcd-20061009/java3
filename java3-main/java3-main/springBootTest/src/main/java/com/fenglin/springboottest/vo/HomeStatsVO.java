package com.fenglin.springboottest.vo;

/**
 * 首页顶部统计：梦境总数、总浏览量、总点赞数、参与作者数
 */
public class HomeStatsVO {

    private long dreamCount;
    private long viewCount;
    private long likeCount;
    private long authorCount;

    public HomeStatsVO() {
    }

    public HomeStatsVO(long dreamCount, long viewCount, long likeCount, long authorCount) {
        this.dreamCount = dreamCount;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.authorCount = authorCount;
    }

    public long getDreamCount() {
        return dreamCount;
    }

    public void setDreamCount(long dreamCount) {
        this.dreamCount = dreamCount;
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

    public long getAuthorCount() {
        return authorCount;
    }

    public void setAuthorCount(long authorCount) {
        this.authorCount = authorCount;
    }
}
