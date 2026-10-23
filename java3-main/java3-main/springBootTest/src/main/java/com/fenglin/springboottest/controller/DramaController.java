package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.DramaDetailDTO;
import com.fenglin.springboottest.dto.DramaQuery;
import com.fenglin.springboottest.service.DramaService;
import com.fenglin.springboottest.vo.CategoryVO;
import com.fenglin.springboottest.vo.DramaVO;
import com.fenglin.springboottest.vo.PageResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广播剧接口：列表（首页展示/分类/排序/关键词/分页）、详情、分类导航。
 * 约定：POST JSON，统一 { code, message, data } 响应。
 * 广播剧为公开内容，浏览/详情/分类均允许匿名访问（无需登录）。
 */
@RestController
@RequestMapping("/api/drama")
public class DramaController {

    private final DramaService dramaService;

    public DramaController(DramaService dramaService) {
        this.dramaService = dramaService;
    }

    /**
     * 广播剧列表：{ page, size, keyword, category, sort }
     * data = { list, total, page, size, pages, hasMore }
     */
    @PostMapping("/list")
    public Result<PageResult<DramaVO>> list(@RequestBody(required = false) DramaQuery query) {
        return Result.ok("ok", dramaService.pageDramas(query));
    }

    /**
     * 广播剧详情：{ id, userId }，浏览量 +1，返回媒体地址（视频/音频）与分类编码
     */
    @PostMapping("/detail")
    public Result<DramaVO> detail(@RequestBody(required = false) DramaDetailDTO dto) {
        Long id = (dto == null) ? null : dto.getId();
        Long userId = (dto == null) ? null : dto.getUserId();
        return Result.ok("ok", dramaService.detail(id, userId));
    }

    /** 分类导航（含各分类已发布的广播剧数量） */
    @PostMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.ok("ok", dramaService.categories());
    }
}
