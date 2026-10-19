package com.fenglin.springboottest.vo;

import com.fenglin.springboottest.entity.Drama;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 广播剧视图对象：列表与详情统一使用。
 * 列表接口与详情接口都返回全部字段（封面/媒体地址等由前端按需取用）。
 */
public class DramaVO {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long id;
    private String title;
    private String summary;
    private String coverUrl;
    private String category;
    /** 分类编码，用于前端配色调性 */
    private String categoryCode;
    /** 标签数组（数据库里是逗号分隔字符串） */
    private List<String> tags;
    /** 视频媒体地址（主播放源，有画面也有声音） */
    private String videoUrl;
    /** 纯音频地址（无视频时回退） */
    private String audioUrl;
    /** 播放器封面 */
    private String posterUrl;
    /** 时长/集数展示 */
    private String duration;
    private Integer views;
    private Integer likes;
    /** 创建时间 yyyy-MM-dd HH:mm */
    private String createTime;
    /** 相对时间：刚刚 / 3 小时前 / 2 天前 */
    private String timeText;

    public static DramaVO of(Drama d) {
        DramaVO vo = new DramaVO();
        vo.id = d.getId();
        vo.title = d.getTitle();
        vo.summary = d.getSummary();
        vo.coverUrl = d.getCoverUrl();
        vo.category = d.getCategory();
        vo.tags = splitTags(d.getTags());
        vo.videoUrl = d.getVideoUrl();
        vo.audioUrl = d.getAudioUrl();
        vo.posterUrl = d.getPosterUrl();
        vo.duration = d.getDuration();
        vo.views = d.getViews() == null ? 0 : d.getViews();
        vo.likes = d.getLikes() == null ? 0 : d.getLikes();
        vo.createTime = d.getCreateTime() == null ? "" : FMT.format(d.getCreateTime());
        vo.timeText = relative(d.getCreateTime());
        return vo;
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

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
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
