package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 广播剧内容表 t_drama —— 首页「广播剧」的数据源（浏览 / 分类 / 详情 / 播放）
 */
@TableName("t_drama")
public class Drama {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 一句话简介 */
    private String summary;

    /** 封面图URL，为空时前端用分类渐变占位 */
    private String coverUrl;

    /** 分类：悬疑/言情/科幻/治愈/古风/儿童 */
    private String category;

    /** 标签，逗号分隔 */
    private String tags;

    /** 来源平台：喜马拉雅 / 猫耳FM / 晋江文学城 / 蜻蜓FM 等热门平台 */
    private String platform;

    /** 视频媒体地址（主播放源，有画面也有声音） */
    private String videoUrl;

    /** 纯音频地址（无视频时回退播放） */
    private String audioUrl;

    /** 播放器封面，为空用分类渐变占位 */
    private String posterUrl;

    /** 时长/集数展示，如 12:34 / 全 12 集 */
    private String duration;

    /** 状态：0 下架，1 发布 */
    private Integer status;

    /** 浏览量 */
    private Integer views;

    /** 点赞数 */
    private Integer likes;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;

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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
