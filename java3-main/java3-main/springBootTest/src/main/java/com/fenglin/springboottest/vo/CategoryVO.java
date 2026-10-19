package com.fenglin.springboottest.vo;

/**
 * 首页分类导航项：分类信息 + 该分类下已发布的梦境数量
 */
public class CategoryVO {

    private Long id;
    private String code;
    private String name;
    private String icon;
    private long count;

    public CategoryVO() {
    }

    public CategoryVO(Long id, String code, String name, String icon, long count) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.count = count;
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

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
