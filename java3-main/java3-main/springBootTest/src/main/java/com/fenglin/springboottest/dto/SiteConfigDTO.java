package com.fenglin.springboottest.dto;

/**
 * 首页栏目配置的保存参数（后台管理）：
 * 有 id 为编辑，无 id 为新增。
 */
public class SiteConfigDTO {

    private Long id;

    /** 配置类型：nav_menu / shortcut / platform / site_text */
    private String configType;

    /** 配置键：同类型内唯一，留空自动生成 */
    private String configKey;

    /** 主文案：导航 label / 快捷入口 title / 平台名 */
    private String label;

    /** 副文案：快捷入口 desc */
    private String subLabel;

    /** 图标标识（快捷入口用） */
    private String iconKey;

    /** 跳转目标 */
    private String target;

    /** 排序号，越小越靠前 */
    private Integer sortNo;

    /** 0 停用，1 启用 */
    private Integer status;

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
}
