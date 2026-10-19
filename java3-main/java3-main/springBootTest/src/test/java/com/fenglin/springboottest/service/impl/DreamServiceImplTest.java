package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenglin.springboottest.common.AuthException;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.common.UserContext;
import com.fenglin.springboottest.dto.DreamActionDTO;
import com.fenglin.springboottest.dto.DreamPublishDTO;
import com.fenglin.springboottest.dto.DreamQuery;
import com.fenglin.springboottest.dto.DreamUpdateDTO;
import com.fenglin.springboottest.entity.Dream;
import com.fenglin.springboottest.entity.DreamCategory;
import com.fenglin.springboottest.entity.DreamFavorite;
import com.fenglin.springboottest.entity.DreamLike;
import com.fenglin.springboottest.entity.DreamSearchLog;
import com.fenglin.springboottest.mapper.DreamCategoryMapper;
import com.fenglin.springboottest.mapper.DreamFavoriteMapper;
import com.fenglin.springboottest.mapper.DreamLikeMapper;
import com.fenglin.springboottest.mapper.DreamMapper;
import com.fenglin.springboottest.mapper.DreamSearchLogMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DreamServiceImpl 单元测试（Mapper 全部 Mock，不依赖数据库）
 * 重点覆盖：登录身份校验、点赞/收藏的增删分支、发布参数归一、分页上限、搜索日志
 */
class DreamServiceImplTest {

    @Mock
    private DreamMapper dreamMapper;
    @Mock
    private DreamCategoryMapper categoryMapper;
    @Mock
    private DreamLikeMapper likeMapper;
    @Mock
    private DreamFavoriteMapper favoriteMapper;
    @Mock
    private DreamSearchLogMapper searchLogMapper;

    private DreamServiceImpl service;

    /**
     * 纯 Mock 单测没有 Spring 容器，MyBatis-Plus 的 lambda 列名缓存（user_id 等）不会被初始化，
     * 一旦物化 SQL 片段就会抛 "can not find lambda cache for this entity"。
     * 这里手动初始化实体元信息缓存，让测试能真正断言生成的 SQL。
     */
    @BeforeAll
    static void initMybatisPlusTableInfo() {
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(new MybatisConfiguration(), "unit-test");
        TableInfoHelper.initTableInfo(assistant, Dream.class);
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DreamServiceImpl(dreamMapper, categoryMapper, likeMapper, favoriteMapper, searchLogMapper);
        // 分类字典默认返回空 → 走白名单兜底逻辑
        when(categoryMapper.selectList(ArgumentMatchers.<Wrapper<DreamCategory>>any()))
                .thenReturn(Collections.emptyList());
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ==================== 登录身份 ====================

    @Test
    @DisplayName("未登录点赞：抛鉴权异常，不写库")
    void toggleLikeWithoutLogin() {
        assertThrows(AuthException.class, () -> service.toggleLike(action(1L)));
        verify(likeMapper, never()).insert(any(DreamLike.class));
    }

    @Test
    @DisplayName("未登录收藏：抛鉴权异常")
    void toggleFavoriteWithoutLogin() {
        assertThrows(AuthException.class, () -> service.toggleFavorite(action(1L)));
        verify(favoriteMapper, never()).insert(any(DreamFavorite.class));
    }

    @Test
    @DisplayName("未登录查看我的收藏：抛鉴权异常")
    void myFavoritesWithoutLogin() {
        assertThrows(AuthException.class, () -> service.myFavorites(new DreamQuery()));
    }

    @Test
    @DisplayName("未登录发布：抛鉴权异常，不写库")
    void publishWithoutLogin() {
        DreamPublishDTO dto = new DreamPublishDTO();
        dto.setTitle("一场很长的梦");
        dto.setContent("正文");
        assertThrows(AuthException.class, () -> service.publish(dto));
        verify(dreamMapper, never()).insert(any(Dream.class));
    }

    // ==================== 点赞 ====================

    @Test
    @DisplayName("首次点赞：写入点赞记录并把点赞数 +1")
    void toggleLikeAddsRecord() {
        login();
        when(dreamMapper.selectById(1L)).thenReturn(dream(1L, 10));
        when(likeMapper.selectOne(any())).thenReturn(null);

        Map<String, Object> result = service.toggleLike(action(1L));

        assertTrue((Boolean) result.get("liked"));
        assertEquals(10, result.get("likes"));
        verify(likeMapper).insert(any(DreamLike.class));
        verify(dreamMapper).update(isNull(), any());
    }

    @Test
    @DisplayName("已点赞再切换：删除记录并把点赞数 -1")
    void toggleLikeRemovesRecord() {
        login();
        when(dreamMapper.selectById(1L)).thenReturn(dream(1L, 10));
        DreamLike exist = new DreamLike();
        exist.setId(88L);
        exist.setDreamId(1L);
        exist.setUserId(9L);
        when(likeMapper.selectOne(any())).thenReturn(exist);

        Map<String, Object> result = service.toggleLike(action(1L));

        assertFalse((Boolean) result.get("liked"));
        verify(likeMapper).deleteById(88L);
        verify(likeMapper, never()).insert(any(DreamLike.class));
    }

    @Test
    @DisplayName("显式 unlike 且未点过：不产生任何写操作")
    void unlikeWhenNotLikedDoesNothing() {
        login();
        when(dreamMapper.selectById(1L)).thenReturn(dream(1L, 10));
        when(likeMapper.selectOne(any())).thenReturn(null);

        DreamActionDTO dto = action(1L);
        dto.setAction("unlike");
        Map<String, Object> result = service.toggleLike(dto);

        assertFalse((Boolean) result.get("liked"));
        verify(likeMapper, never()).insert(any(DreamLike.class));
        verify(dreamMapper, never()).update(isNull(), any());
    }

    @Test
    @DisplayName("点赞不存在的梦境：抛业务异常")
    void toggleLikeMissingDream() {
        login();
        when(dreamMapper.selectById(999L)).thenReturn(null);
        assertThrows(BizException.class, () -> service.toggleLike(action(999L)));
    }

    // ==================== 收藏 ====================

    @Test
    @DisplayName("首次收藏：写入收藏记录并返回 favorited=true")
    void toggleFavoriteAddsRecord() {
        login();
        when(dreamMapper.selectById(1L)).thenReturn(dream(1L, 10));
        when(favoriteMapper.selectOne(any())).thenReturn(null);

        Map<String, Object> result = service.toggleFavorite(action(1L));

        assertTrue((Boolean) result.get("favorited"));
        verify(favoriteMapper).insert(any(DreamFavorite.class));
    }

    @Test
    @DisplayName("已收藏再切换：删除收藏记录")
    void toggleFavoriteRemovesRecord() {
        login();
        when(dreamMapper.selectById(1L)).thenReturn(dream(1L, 10));
        DreamFavorite exist = new DreamFavorite();
        exist.setId(66L);
        exist.setDreamId(1L);
        exist.setUserId(9L);
        when(favoriteMapper.selectOne(any())).thenReturn(exist);

        Map<String, Object> result = service.toggleFavorite(action(1L));

        assertFalse((Boolean) result.get("favorited"));
        verify(favoriteMapper).deleteById(66L);
    }

    // ==================== 列表 / 搜索 ====================

    @Test
    @DisplayName("分页 size 超过上限时被钳制到 50")
    @SuppressWarnings("unchecked")
    void pageSizeIsCapped() {
        DreamQuery query = new DreamQuery();
        query.setSize(999);
        doReturn(emptyPage()).when(dreamMapper).selectPage(any(), any());

        service.pageDreams(query);

        ArgumentCaptor<IPage<Dream>> captor = ArgumentCaptor.forClass(IPage.class);
        verify(dreamMapper).selectPage(captor.capture(), any());
        assertEquals(50, captor.getValue().getSize());
    }

    @Test
    @DisplayName("关键词搜索：写入搜索日志（关键词为空时不写）")
    void searchRecordsKeywordLog() {
        doReturn(emptyPage()).when(dreamMapper).selectPage(any(), any());

        DreamQuery withKeyword = new DreamQuery();
        withKeyword.setKeyword("  星星  ");
        service.search(withKeyword);
        ArgumentCaptor<DreamSearchLog> captor = ArgumentCaptor.forClass(DreamSearchLog.class);
        verify(searchLogMapper).insert(captor.capture());
        assertEquals("星星", captor.getValue().getKeyword());
        assertEquals(0L, captor.getValue().getUserId());

        DreamQuery blank = new DreamQuery();
        blank.setKeyword("   ");
        service.search(blank);
        // 关键词为空不记录：两次搜索后仍只应有第一次那一条日志
        verify(searchLogMapper, times(1)).insert(any(DreamSearchLog.class));
    }

    // ==================== 发布 ====================

    @Test
    @DisplayName("发布：作者身份取登录态、非法分类回落随笔、标签去重")
    void publishNormalizesFields() {
        login();
        DreamPublishDTO dto = new DreamPublishDTO();
        dto.setTitle("接口测试写下的一场梦");
        dto.setContent("月光落在窗台上，像一层很薄的雪。");
        dto.setTags("月亮,月亮,  雪 ,");
        dto.setCategory("不存在的分类");
        dto.setUserId(12345L);          // 请求体里的 userId 应被忽略

        Dream saved = service.publish(dto);

        assertEquals(9L, saved.getUserId());
        assertEquals("梦梦", saved.getAuthorName());
        assertEquals("随笔", saved.getCategory());
        assertEquals("月亮,雪", saved.getTags());
        // 摘要为空时取正文前 60 字
        assertEquals("月光落在窗台上，像一层很薄的雪。", saved.getSummary());
        assertEquals(1, saved.getStatus());
        assertEquals(0, saved.getViews());
        verify(dreamMapper).insert(any(Dream.class));
    }

    @Test
    @DisplayName("发布：标题过短或正文为空都抛业务异常")
    void publishValidatesTitleAndContent() {
        login();
        DreamPublishDTO shortTitle = new DreamPublishDTO();
        shortTitle.setTitle("短");
        shortTitle.setContent("正文");
        assertThrows(BizException.class, () -> service.publish(shortTitle));

        DreamPublishDTO noContent = new DreamPublishDTO();
        noContent.setTitle("一场正常的梦");
        noContent.setContent("   ");
        assertThrows(BizException.class, () -> service.publish(noContent));

        verify(dreamMapper, never()).insert(any(Dream.class));
    }

    // ==================== 编辑 / 删除 / 我的梦境 / 个人中心 ====================

    @Test
    @DisplayName("编辑别人的梦境：拒绝，不落库")
    void updateRejectsOthersDream() {
        login();
        Dream others = dream(1L, 10);
        others.setUserId(5L);          // 作者是别人
        when(dreamMapper.selectById(1L)).thenReturn(others);

        DreamUpdateDTO dto = new DreamUpdateDTO();
        dto.setId(1L);
        dto.setTitle("偷偷改别人的梦");
        dto.setContent("正文");

        BizException e = assertThrows(BizException.class, () -> service.update(dto));
        assertEquals("只能编辑自己发布的梦境", e.getMessage());
        verify(dreamMapper, never()).updateById(any(Dream.class));
    }

    @Test
    @DisplayName("编辑自己的梦境：字段归一后落库")
    void updateOwnDream() {
        login();
        Dream mine = dream(1L, 10);
        mine.setUserId(9L);
        when(dreamMapper.selectById(1L)).thenReturn(mine);

        DreamUpdateDTO dto = new DreamUpdateDTO();
        dto.setId(1L);
        dto.setTitle("  改好的标题  ");
        dto.setContent("改好的正文");
        dto.setCategory("不存在");
        dto.setTags("海,海,风");

        Dream saved = service.update(dto);

        assertEquals("改好的标题", saved.getTitle());
        assertEquals("随笔", saved.getCategory());
        assertEquals("海,风", saved.getTags());
        assertEquals("改好的正文", saved.getSummary());   // 摘要为空 → 取正文前 60 字
        verify(dreamMapper).updateById(any(Dream.class));
    }

    @Test
    @DisplayName("删除别人的梦境：拒绝，不删除")
    void deleteRejectsOthersDream() {
        login();
        Dream others = dream(1L, 10);
        others.setUserId(5L);
        when(dreamMapper.selectById(1L)).thenReturn(others);

        BizException e = assertThrows(BizException.class, () -> service.deleteDream(1L));
        assertEquals("只能删除自己发布的梦境", e.getMessage());
        verify(dreamMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("删除自己的梦境：返回标题并执行删除")
    void deleteOwnDream() {
        login();
        Dream mine = dream(1L, 10);
        mine.setUserId(9L);
        when(dreamMapper.selectById(1L)).thenReturn(mine);

        assertEquals("在云端城堡的午后", service.deleteDream(1L));
        verify(dreamMapper).deleteById(1L);
    }

    @Test
    @DisplayName("我的梦境：强制按登录用户过滤，忽略外部传入的 authorId")
    @SuppressWarnings("unchecked")
    void myDreamsForcesLoginUser() {
        login();
        doReturn(emptyPage()).when(dreamMapper).selectPage(any(), any());

        DreamQuery query = new DreamQuery();
        query.setAuthorId(777L);          // 想偷看别人的内容
        service.myDreams(query);

        ArgumentCaptor<Wrapper<Dream>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(dreamMapper).selectPage(any(), captor.capture());
        LambdaQueryWrapper<Dream> wrapper = (LambdaQueryWrapper<Dream>) captor.getValue();
        String sql = wrapper.getCustomSqlSegment();
        Map<String, Object> params = wrapper.getParamNameValuePairs();

        // 生成的 SQL 片段必须带 user_id 过滤（status + user_id 两个条件）
        assertTrue(sql.contains("user_id"), "「我的梦境」应强制带 user_id 过滤，实际：" + sql);
        assertEquals(2, params.size(), "条件数应为 2（status + user_id），实际：" + params);
        assertTrue(params.containsValue(9L), "应按登录用户 9 过滤，实际：" + params);
        assertFalse(params.containsValue(777L), "不应使用外部传入的 authorId，实际：" + params);
    }

    @Test
    @DisplayName("个人中心统计：梦境数 / 浏览 / 获赞 / 被收藏")
    void profileStats() {
        login();

        List<Object> ids = new ArrayList<>();
        ids.add(1L);
        ids.add(2L);
        ids.add(3L);
        when(dreamMapper.selectObjs(any())).thenReturn(ids);
        when(favoriteMapper.selectCount(any())).thenReturn(5L);
        Map<String, Object> sum = new java.util.HashMap<>();
        sum.put("view_sum", 1234L);
        sum.put("like_sum", 56L);
        when(dreamMapper.selectMaps(any())).thenReturn(Collections.singletonList(sum));

        Map<String, Object> profile = service.profile();

        assertEquals(9L, profile.get("userId"));
        assertEquals("梦梦", profile.get("username"));
        assertEquals(3, profile.get("dreamCount"));
        assertEquals(1234L, profile.get("viewCount"));
        assertEquals(56L, profile.get("likeCount"));
        assertEquals(5L, profile.get("favoriteCount"));
    }

    // ==================== 辅助方法 ====================

    /** 模拟登录：写入 UserContext（拦截器在真实请求里做的事） */
    private void login() {
        UserContext.set(9L, "梦梦");
    }

    private DreamActionDTO action(Long id) {
        DreamActionDTO dto = new DreamActionDTO();
        dto.setId(id);
        return dto;
    }

    private Dream dream(Long id, int likes) {
        Dream d = new Dream();
        d.setId(id);
        d.setUserId(1L);
        d.setAuthorName("梦梦");
        d.setTitle("在云端城堡的午后");
        d.setSummary("摘要");
        d.setContent("正文");
        d.setCategory("美梦");
        d.setTags("云,午后");
        d.setStatus(1);
        d.setViews(3);
        d.setLikes(likes);
        d.setCreateTime(LocalDateTime.now().minusHours(2));
        return d;
    }

    private Page<Dream> emptyPage() {
        Page<Dream> page = new Page<>(1, 10);
        page.setRecords(Collections.emptyList());
        page.setTotal(0);
        return page;
    }
}
