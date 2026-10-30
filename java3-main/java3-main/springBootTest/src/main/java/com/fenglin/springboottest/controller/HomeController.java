package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.DreamQuery;
import com.fenglin.springboottest.service.DreamService;
import com.fenglin.springboottest.service.SiteConfigService;
import com.fenglin.springboottest.vo.CategoryVO;
import com.fenglin.springboottest.vo.DreamVO;
import com.fenglin.springboottest.vo.HomeOverviewVO;
import com.fenglin.springboottest.vo.HotKeywordVO;
import com.fenglin.springboottest.vo.PageResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 首页展示接口：聚合数据、内容流、分类导航、热门搜索词、首页栏目配置。
 * 约定：POST JSON，统一 { code, message, data } 响应。
 */
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final DreamService dreamService;
    private final SiteConfigService siteConfigService;

    public HomeController(DreamService dreamService, SiteConfigService siteConfigService) {
        this.dreamService = dreamService;
        this.siteConfigService = siteConfigService;
    }

    /**
     * 首页聚合数据（一次请求拿全）：{ userId }
     * data = { stats, categories, hotKeywords, hotDreams, latestDreams }
     */
    @PostMapping("/overview")
    public Result<HomeOverviewVO> overview(@RequestBody(required = false) DreamQuery query) {
        Long userId = (query == null) ? null : query.getUserId();
        return Result.ok("ok", dreamService.overview(userId));
    }

    /**
     * 首页内容流（分页）：{ page, size, keyword, category, sort, userId }
     * sort=newest 最新 / hot 最热；category 传分类名或编码，不传或 "全部" 为不限
     */
    @PostMapping("/feed")
    public Result<PageResult<DreamVO>> feed(@RequestBody(required = false) DreamQuery query) {
        return Result.ok("ok", dreamService.pageDreams(query));
    }

    /** 分类导航（含各分类内容数量） */
    @PostMapping("/categories")
    public Result<List<CategoryVO>> categories() {
        return Result.ok("ok", dreamService.categories());
    }

    /** 热门搜索词 */
    @PostMapping("/hot-keywords")
    public Result<List<HotKeywordVO>> hotKeywords() {
        return Result.ok("ok", dreamService.hotKeywords());
    }

    /**
     * 首页栏目配置（匿名可访问，由后台管理系统维护）。
     * data = {
     *   navMenu:  [ { key, label, target } ],
     *   shortcut: [ { key, title, desc, iconKey, target } ],
     *   platform: [ "喜马拉雅", ... ],
     *   text:     { brand_name, hero_title, hero_sub, footer }
     * }
     * 接口异常时前端会回落到内置默认值，不会因为后端故障导致首页开天窗。
     */
    @PostMapping("/config")
    public Result<Map<String, Object>> config() {
        return Result.ok("ok", siteConfigService.frontConfig());
    }
}
