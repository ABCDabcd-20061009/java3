package com.fenglin.springboottest.vo;

/**
 * 后台分类视图（梦境分类与广播剧分类通用）
 */
public class AdminCategoryVO {

    private Long id;
    private String code;
    private String name;
    private String icon;
    private Integer sortNo;
    private Integer status;

    public AdminCategoryVO() {
    }

    public AdminCategoryVO(Long id, String code, String name, String icon, Integer sortNo, Integer status) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.sortNo = sortNo;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
