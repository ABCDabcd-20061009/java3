package com.fenglin.springboottest.vo;

import com.fenglin.springboottest.entity.Dream;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 首页展示 / 搜索 / 详情统一的梦境视图对象
 * 列表接口会清空 content（省流量），详情接口才返回正文。
 */
public class DreamVO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private Long userId;
    private String authorName;
    /** 头像文字：昵称首字 */
    private String avatarText;
    private String title;
    private String summary;
    /** 正文，仅详情接口返回 */
    private String content;
    private String coverUrl;
    private String category;
    /** 分类编码，用于前端配色调性 */
    private String categoryCode;
    /** 标签数组（数据库里是逗号分隔字符串） */
    private List<String> tags;
    private Integer views;
    private Integer likes;
    /** 当前登录用户是否已点赞 */
    private boolean liked;
    /** 当前登录用户是否已收藏 */
    private boolean favorited;
    /** 创建时间 yyyy-MM-dd HH:mm */
    private String createTime;
    /** 相对时间：刚刚 / 3 小时前 / 2 天前 */
    private String timeText;

    public static DreamVO of(Dream dream) {
        DreamVO vo = new DreamVO();
        vo.id = dream.getId();
        vo.userId = dream.getUserId();
        vo.authorName = dream.getAuthorName();
        vo.avatarText = initialOf(dream.getAuthorName());
        vo.title = dream.getTitle();
        vo.summary = dream.getSummary();
        vo.content = dream.getContent();
        vo.coverUrl = dream.getCoverUrl();
        vo.category = dream.getCategory();
        vo.tags = splitTags(dream.getTags());
        vo.views = dream.getViews() == null ? 0 : dream.getViews();
        vo.likes = dream.getLikes() == null ? 0 : dream.getLikes();
        vo.createTime = dream.getCreateTime() == null ? "" : FMT.format(dream.getCreateTime());
        vo.timeText = relative(dream.getCreateTime());
        return vo;
    }

    /** 列表场景使用：去掉正文 */
    public DreamVO withoutContent() {
        this.content = null;
        return this;
    }

    private static String initialOf(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "梦";
        }
        return name.trim().substring(0, 1);
    }

    private static List<String> splitTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static String relative(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Duration d = Duration.between(time, LocalDateTime.now());
        long minutes = d.toMinutes();
        if (minutes < 1) {
            return "刚刚";
        }
        if (minutes < 60) {
            return minutes + " 分钟前";
        }
        long hours = d.toHours();
        if (hours < 24) {
            return hours + " 小时前";
        }
        long days = d.toDays();
        if (days < 30) {
            return days + " 天前";
        }
        return FMT.format(time);
    }

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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAvatarText() {
        return avatarText;
    }

    public void setAvatarText(String avatarText) {
        this.avatarText = avatarText;
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

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }
}
