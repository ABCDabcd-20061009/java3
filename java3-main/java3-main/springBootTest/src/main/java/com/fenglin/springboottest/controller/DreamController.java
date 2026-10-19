package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.DreamActionDTO;
import com.fenglin.springboottest.dto.DreamDetailDTO;
import com.fenglin.springboottest.dto.DreamPublishDTO;
import com.fenglin.springboottest.dto.DreamQuery;
import com.fenglin.springboottest.dto.DreamUpdateDTO;
import com.fenglin.springboottest.entity.Dream;
import com.fenglin.springboottest.service.DreamService;
import com.fenglin.springboottest.vo.CategoryVO;
import com.fenglin.springboottest.vo.DreamVO;
import com.fenglin.springboottest.vo.HotKeywordVO;
import com.fenglin.springboottest.vo.PageResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 梦境内容接口：列表（首页展示/搜索/分类/排序/分页）、详情、点赞、发布。
 * 约定：POST JSON，统一 { code, message, data } 响应。
 */
@RestController
@RequestMapping("/api/dream")
public class DreamController {

    private final DreamService dreamService;

    public DreamController(DreamService dreamService) {
        this.dreamService = dreamService;
    }

    /**
     * 首页列表 / 搜索：{ page, size, keyword, category, sort, userId }
     * data = { list, total, page, size, pages, hasMore }
     */
    @PostMapping("/list")
    public Result<PageResult<DreamVO>> list(@RequestBody(required = false) DreamQuery query) {
        return Result.ok("ok", dreamService.pageDreams(query));
    }

    /**
     * 搜索（与 /api/search 等价，会记录搜索日志用于热门搜索词统计）
     */
    @PostMapping("/search")
    public Result<PageResult<DreamVO>> search(@RequestBody(required = false) DreamQuery query) {
        return Result.ok("ok", dreamService.search(query));
    }

    /**
     * 梦境详情：{ id, userId }，浏览量 +1，返回正文与「我是否已点赞」
     */
    @PostMapping("/detail")
    public Result<DreamVO> detail(@RequestBody DreamDetailDTO dto) {
        Long id = (dto == null) ? null : dto.getId();
        Long userId = (dto == null) ? null : dto.getUserId();
        return Result.ok("ok", dreamService.detail(id, userId));
    }

    /**
     * 点赞 / 取消点赞：{ id, action: like|unlike|toggle }（需登录，身份取自 token）
     * data = { id, liked, likes }
     */
    @PostMapping("/like")
    public Result<Map<String, Object>> like(@RequestBody DreamActionDTO dto) {
        return Result.ok("ok", dreamService.toggleLike(dto));
    }

    /**
     * 收藏 / 取消收藏：{ id, action: favorite|unfavorite|toggle }（需登录，身份取自 token）
     * data = { id, favorited }
     */
    @PostMapping("/favorite")
    public Result<Map<String, Object>> favorite(@RequestBody DreamActionDTO dto) {
        return Result.ok("ok", dreamService.toggleFavorite(dto));
    }

    /** 分类导航（含各分类数量） */
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
     * 发布梦境：{ title, summary, content, coverUrl, category, tags }（需登录，作者取 token 身份）
     */
    @PostMapping("/publish")
    public Result<Map<String, Object>> publish(@RequestBody DreamPublishDTO dto) {
        return Result.ok("发布成功", toSimple(dreamService.publish(dto)));
    }

    /**
     * 编辑自己发布的梦境：{ id, title, summary, content, coverUrl, category, tags }（需登录）
     */
    @PostMapping("/update")
    public Result<Map<String, Object>> update(@RequestBody DreamUpdateDTO dto) {
        return Result.ok("修改成功", toSimple(dreamService.update(dto)));
    }

    /**
     * 删除自己发布的梦境：{ id }（需登录，逻辑删除）
     * data = { id, title }
     */
    @PostMapping("/delete")
    public Result<Map<String, Object>> delete(@RequestBody DreamDetailDTO dto) {
        Long id = (dto == null) ? null : dto.getId();
        String title = dreamService.deleteDream(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", title);
        return Result.ok("已删除", data);
    }

    private Map<String, Object> toSimple(Dream dream) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", dream.getId());
        data.put("title", dream.getTitle());
        data.put("category", dream.getCategory());
        data.put("authorName", dream.getAuthorName());
        return data;
    }
}
