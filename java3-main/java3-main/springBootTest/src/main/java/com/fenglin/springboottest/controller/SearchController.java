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

/**
 * 首页搜索接口。
 * 对齐前端 api.js 里预留的契约：POST /api/search { keyword, page, size, category, sort, userId }
 * 返回 data = { list, total, page, size, pages, hasMore }
 */
@RestController
@RequestMapping("/api")
public class SearchController {

    private final DreamService dreamService;

    public SearchController(DreamService dreamService) {
        this.dreamService = dreamService;
    }

    @PostMapping("/search")
    public Result<PageResult<DreamVO>> search(@RequestBody(required = false) DreamQuery query) {
        return Result.ok("ok", dreamService.search(query));
    }
}
