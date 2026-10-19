package com.fenglin.springboottest.vo;

import java.util.Collections;
import java.util.List;

/**
 * 首页聚合数据：一次请求拿到首页展示所需的全部内容
 * { stats, categories, hotKeywords, hotDreams, latestDreams }
 */
public class HomeOverviewVO {

    /** 顶部统计 */
    private HomeStatsVO stats;

    /** 分类导航（含各分类数量） */
    private List<CategoryVO> categories = Collections.emptyList();

    /** 热门搜索词 */
    private List<HotKeywordVO> hotKeywords = Collections.emptyList();

    /** 热门推荐（按浏览量倒序） */
    private List<DreamVO> hotDreams = Collections.emptyList();

    /** 最新上架（按时间倒序） */
    private List<DreamVO> latestDreams = Collections.emptyList();

    public HomeStatsVO getStats() {
        return stats;
    }

    public void setStats(HomeStatsVO stats) {
        this.stats = stats;
    }

    public List<CategoryVO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryVO> categories) {
        this.categories = categories;
    }

    public List<HotKeywordVO> getHotKeywords() {
        return hotKeywords;
    }

    public void setHotKeywords(List<HotKeywordVO> hotKeywords) {
        this.hotKeywords = hotKeywords;
    }

    public List<DreamVO> getHotDreams() {
        return hotDreams;
    }

    public void setHotDreams(List<DreamVO> hotDreams) {
        this.hotDreams = hotDreams;
    }

    public List<DreamVO> getLatestDreams() {
        return latestDreams;
    }

    public void setLatestDreams(List<DreamVO> latestDreams) {
        this.latestDreams = latestDreams;
    }
}
