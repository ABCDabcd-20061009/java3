package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.DreamActionDTO;
import com.fenglin.springboottest.dto.DreamPublishDTO;
import com.fenglin.springboottest.dto.DreamQuery;
import com.fenglin.springboottest.dto.DreamUpdateDTO;
import com.fenglin.springboottest.entity.Dream;
import com.fenglin.springboottest.vo.CategoryVO;
import com.fenglin.springboottest.vo.DreamVO;
import com.fenglin.springboottest.vo.HomeOverviewVO;
import com.fenglin.springboottest.vo.HotKeywordVO;
import com.fenglin.springboottest.vo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 首页内容服务：首页展示（列表/聚合/分类） + 搜索 + 详情 + 点赞 + 收藏 + 发布
 * 说明：点赞、收藏、发布一律以 JWT 解析出的登录身份为准（见 common/UserContext）。
 */
public interface DreamService {

    /**
     * 首页列表：支持关键词搜索、分类筛选、排序与分页。
     * 只返回已发布（status=1）且未删除的内容；登录后附带 liked / favorited。
     */
    PageResult<DreamVO> pageDreams(DreamQuery query);

    /**
     * 关键词搜索：在首页列表基础上，额外把关键词记入搜索日志（用于热门搜索词统计）。
     */
    PageResult<DreamVO> search(DreamQuery query);

    /**
     * 首页聚合数据：统计 + 分类导航 + 热门搜索词 + 热门推荐 + 最新上架。
     */
    HomeOverviewVO overview(Long userId);

    /**
     * 分类导航（含各分类内容数量）。
     */
    List<CategoryVO> categories();

    /**
     * 热门搜索词：取近 30 天搜索日志的 topN，无数据时返回默认推荐词。
     */
    List<HotKeywordVO> hotKeywords();

    /**
     * 梦境详情：返回记录并把浏览量 +1（原子自增），带 liked / favorited。
     */
    DreamVO detail(Long id, Long userId);

    /**
     * 点赞 / 取消点赞 / 切换点赞（需登录）。
     * action：like / unlike / toggle（默认）
     *
     * @return { id, liked, likes }
     */
    Map<String, Object> toggleLike(DreamActionDTO dto);

    /**
     * 收藏 / 取消收藏 / 切换收藏（需登录）。
     * action：favorite / unfavorite / toggle（默认）
     *
     * @return { id, favorited }
     */
    Map<String, Object> toggleFavorite(DreamActionDTO dto);

    /**
     * 我的收藏列表（需登录），按收藏时间倒序分页。
     */
    PageResult<DreamVO> myFavorites(DreamQuery query);

    /**
     * 发布一条梦境（需登录，发布状态，立即出现在首页）。
     */
    Dream publish(DreamPublishDTO dto);

    /**
     * 我的梦境（需登录）：只返回自己发布的内容，带正文方便直接编辑。
     */
    PageResult<DreamVO> myDreams(DreamQuery query);

    /**
     * 编辑自己发布的梦境（需登录，非本人内容拒绝）。
     */
    Dream update(DreamUpdateDTO dto);

    /**
     * 删除自己发布的梦境（需登录，非本人内容拒绝；逻辑删除，不物理删行）。
     *
     * @return 被删除的梦境标题（供前端提示）
     */
    String deleteDream(Long id);

    /**
     * 个人中心统计（需登录）：我的梦境数、累计浏览、累计获赞、被收藏数。
     */
    Map<String, Object> profile();
}
