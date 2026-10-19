package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.DreamQuery;
import com.fenglin.springboottest.service.DreamService;
import com.fenglin.springboottest.vo.DreamVO;
import com.fenglin.springboottest.vo.PageResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 用户相关的首页接口（需登录，身份由 JwtInterceptor 解析）。
 * 注意：类名避开根包下遗留的 Thymeleaf 演示 UserController。
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    private final DreamService dreamService;

    public UserApiController(DreamService dreamService) {
        this.dreamService = dreamService;
    }

    /**
     * 我的收藏：{ page, size }，按收藏时间倒序
     * data = { list, total, page, size, pages, hasMore }
     */
    @PostMapping("/favorites")
    public Result<PageResult<DreamVO>> favorites(@RequestBody(required = false) DreamQuery query) {
        return Result.ok("ok", dreamService.myFavorites(query));
    }

    /**
     * 我的梦境：{ page, size, keyword?, sort? }，只返回自己发布的内容（带正文，便于直接编辑）
     */
    @PostMapping("/dreams")
    public Result<PageResult<DreamVO>> myDreams(@RequestBody(required = false) DreamQuery query) {
        return Result.ok("ok", dreamService.myDreams(query));
    }

    /**
     * 个人中心统计：{ userId, username, dreamCount, viewCount, likeCount, favoriteCount }
     */
    @PostMapping("/profile")
    public Result<Map<String, Object>> profile() {
        return Result.ok("ok", dreamService.profile());
    }
}
