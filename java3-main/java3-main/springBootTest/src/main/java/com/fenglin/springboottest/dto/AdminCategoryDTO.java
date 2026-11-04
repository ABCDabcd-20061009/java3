package com.fenglin.springboottest.dto;

/**
 * 后台分类保存参数：type = dream（梦境分类 t_dream_category）/ drama（广播剧分类 t_drama_category）
 */
public class AdminCategoryDTO {

    private Long id;
    /** dream / drama */
    private String type;
    /** 分类编码，留空自动生成 */
    private String code;
    /** 分类名称 */
    private String name;
    /** 图标标识，前端映射 SVG */
    private String icon;
    /** 排序号 */
    private Integer sortNo;
    /** 0 停用，1 启用 */
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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
