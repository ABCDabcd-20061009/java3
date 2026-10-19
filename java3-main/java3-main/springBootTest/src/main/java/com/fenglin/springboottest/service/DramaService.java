package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.DramaDetailDTO;
import com.fenglin.springboottest.dto.DramaQuery;
import com.fenglin.springboottest.vo.CategoryVO;
import com.fenglin.springboottest.vo.DramaVO;
import com.fenglin.springboottest.vo.PageResult;

import java.util.List;

/**
 * 广播剧服务：浏览列表（分类/排序/关键词/分页）、详情（浏览量+1）、分类导航。
 * 说明：广播剧为公开内容，浏览/详情/分类均允许匿名访问（无需登录）。
 */
public interface DramaService {

    /**
     * 广播剧列表：支持关键词、分类筛选、排序与分页。
     * 只返回已发布（status=1）且未删除的广播剧。
     */
    PageResult<DramaVO> pageDramas(DramaQuery query);

    /**
     * 广播剧详情：返回记录并把浏览量 +1（原子自增）。
     */
    DramaVO detail(Long id, Long userId);

    /**
     * 分类导航（含各分类已发布的广播剧数量）。
     */
    List<CategoryVO> categories();
}
