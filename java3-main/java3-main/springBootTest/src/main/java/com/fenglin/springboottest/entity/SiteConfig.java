package com.fenglin.springboottest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 首页栏目配置表 t_site_config —— 首页导航菜单 / 快捷入口 / 广播剧平台 / 站点文案
 *
 * 四类 config_type：
 * - nav_menu  导航菜单（key/label/target）
 * - shortcut  快捷入口（title/desc/iconKey/target）
 * - platform  广播剧平台（名称列表）
 * - site_text 站点文案（键值：brand_name / hero_title / hero_sub / footer）
 */
@TableName("t_site_config")
public class SiteConfig {

    /** 导航菜单 */
    public static final String TYPE_NAV_MENU = "nav_menu";
    /** 快捷入口 */
    public static final String TYPE_SHORTCUT = "shortcut";
    /** 广播剧平台 */
    public static final String TYPE_PLATFORM = "platform";
    /** 站点文案 */
    public static final String TYPE_SITE_TEXT = "site_text";

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置类型：nav_menu / shortcut / platform / site_text */
    private String configType;

    /** 配置键：同类型内唯一；site_text 固定为 brand_name 等文案键 */
    private String configKey;

    /** 主文案：导航 label / 快捷入口 title / 平台名 */
    private String label;

    /** 副文案：快捷入口 desc */
    private String subLabel;

    /** 图标标识，前端按 key 映射预置 SVG */
    private String iconKey;

    /** 跳转目标：square / favorites / profile / drama / help */
    private String target;

    /** 排序号，越小越靠前 */
    private Integer sortNo;

    /** 0 停用，1 启用（停用后前台 /home/config 不再返回） */
    private Integer status;

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

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getSubLabel() {
        return subLabel;
    }

    public void setSubLabel(String subLabel) {
        this.subLabel = subLabel;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
