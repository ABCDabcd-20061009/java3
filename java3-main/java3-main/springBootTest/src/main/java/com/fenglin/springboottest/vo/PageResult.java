package com.fenglin.springboottest.vo;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果：前端首页列表 / 搜索结果统一使用
 * { list, total, page, size, pages, hasMore }
 */
public class PageResult<T> {

    /** 当前页数据 */
    private List<T> list;

    /** 总条数 */
    private long total;

    /** 当前页码，从 1 开始 */
    private long page;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    /** 是否还有下一页 */
    private boolean hasMore;

    public PageResult() {
        this.list = Collections.emptyList();
    }

    public PageResult(List<T> list, long total, long page, long size) {
        this.list = (list == null) ? Collections.emptyList() : list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (size <= 0) ? 0 : (total + size - 1) / size;
        this.hasMore = page < this.pages;
    }

    public static <T> PageResult<T> empty(long page, long size) {
        return new PageResult<>(Collections.emptyList(), 0, page, size);
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
