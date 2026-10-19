package com.fenglin.springboottest.vo;

/**
 * 热门搜索词项：关键词 + 近 30 天被搜索次数
 */
public class HotKeywordVO {

    private String keyword;
    private long count;

    public HotKeywordVO() {
    }

    public HotKeywordVO(String keyword, long count) {
        this.keyword = keyword;
        this.count = count;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
