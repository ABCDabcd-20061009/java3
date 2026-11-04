package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.AdminCategoryDTO;
import com.fenglin.springboottest.dto.AdminDreamDTO;
import com.fenglin.springboottest.dto.AdminDramaDTO;
import com.fenglin.springboottest.entity.Admin;
import com.fenglin.springboottest.entity.Dream;
import com.fenglin.springboottest.entity.DreamCategory;
import com.fenglin.springboottest.entity.Drama;
import com.fenglin.springboottest.entity.DramaCategory;
import com.fenglin.springboottest.entity.DreamSearchLog;
import com.fenglin.springboottest.entity.User;
import com.fenglin.springboottest.mapper.AdminMapper;
import com.fenglin.springboottest.mapper.DreamCategoryMapper;
import com.fenglin.springboottest.mapper.DreamFavoriteMapper;
import com.fenglin.springboottest.mapper.DreamLikeMapper;
import com.fenglin.springboottest.mapper.DreamMapper;
import com.fenglin.springboottest.mapper.DreamSearchLogMapper;
import com.fenglin.springboottest.mapper.DramaCategoryMapper;
import com.fenglin.springboottest.mapper.DramaMapper;
import com.fenglin.springboottest.mapper.UserMapper;
import com.fenglin.springboottest.service.AdminService;
import com.fenglin.springboottest.util.JwtUtil;
import com.fenglin.springboottest.util.PasswordUtil;
import com.fenglin.springboottest.vo.AdminCategoryVO;
import com.fenglin.springboottest.vo.AdminStatsVO;
import com.fenglin.springboottest.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理实现。约定：
 * - 列表返回 { list, total, page, size, pages, hasMore }
 * - 返回体手工用 LinkedHashMap 组装，不直接抛实体，规避 LocalDateTime 被 Jackson 序列化成数组
 * - 梦境/广播剧/用户删除均为逻辑删除（实体上 @TableLogic 自动生效）
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /** 单次返回上限，防止误传超大 size */
    private static final int MAX_SIZE = 100;

    private final AdminMapper adminMapper;
    private final DreamMapper dreamMapper;
    private final DramaMapper dramaMapper;
    private final DreamCategoryMapper dreamCategoryMapper;
    private final DramaCategoryMapper dramaCategoryMapper;
    private final UserMapper userMapper;
    private final DreamSearchLogMapper searchLogMapper;
    private final DreamLikeMapper likeMapper;
    private final DreamFavoriteMapper favoriteMapper;
    private final JwtUtil jwtUtil;

    public AdminServiceImpl(AdminMapper adminMapper,
                            DreamMapper dreamMapper,
                            DramaMapper dramaMapper,
                            DreamCategoryMapper dreamCategoryMapper,
                            DramaCategoryMapper dramaCategoryMapper,
                            UserMapper userMapper,
                            DreamSearchLogMapper searchLogMapper,
                            DreamLikeMapper likeMapper,
                            DreamFavoriteMapper favoriteMapper,
                            JwtUtil jwtUtil) {
        this.adminMapper = adminMapper;
        this.dreamMapper = dreamMapper;
        this.dramaMapper = dramaMapper;
        this.dreamCategoryMapper = dreamCategoryMapper;
        this.dramaCategoryMapper = dramaCategoryMapper;
        this.userMapper = userMapper;
        this.searchLogMapper = searchLogMapper;
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
        this.jwtUtil = jwtUtil;
    }

    // ==================== 登录 / 信息 / 概览 ====================

    @Override
    public Map<String, Object> login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BizException("请输入用户名和密码");
        }
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username.trim())
                .last("limit 1"));
        if (admin == null || !PasswordUtil.matches(password, admin.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        String token = jwtUtil.generate(admin.getId(), admin.getUsername(), JwtUtil.ROLE_ADMIN);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        data.put("token", token);
        return data;
    }

    @Override
    public Map<String, Object> info() {
        Long id = requireAdminId();
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BizException("管理员不存在");
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        return data;
    }

    @Override
    public AdminStatsVO overview() {
        AdminStatsVO vo = new AdminStatsVO();
        vo.setDreamCount(dreamMapper.selectCount(null));
        vo.setDreamPublishedCount(dreamMapper.selectCount(
                new LambdaQueryWrapper<Dream>().eq(Dream::getStatus, 1)));
        vo.setDramaCount(dramaMapper.selectCount(null));
        vo.setUserCount(userMapper.selectCount(null));
        vo.setViewCount(sumOf("t_dream", "views"));
        vo.setLikeCount(sumOf("t_dream", "likes"));
        vo.setFavoriteCount(favoriteMapper.selectCount(null));
        vo.setSearchCount(searchLogMapper.selectCount(null));
        return vo;
    }

    private long sumOf(String table, String column) {
        // 表名/列名都是代码内常量，无注入风险
        Long sum = dreamMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Dream>()
                        .select("IFNULL(SUM(" + column + "),0) AS total")).get(0)
                .values().stream().findFirst().map(v -> Long.valueOf(String.valueOf(v))).orElse(0L);
        return sum == null ? 0L : sum;
    }

    // ==================== 梦境管理 ====================

    @Override
    public Map<String, Object> pageDreams(Integer page, Integer size, String keyword, String category, Integer status) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Dream::getTitle, kw).or().like(Dream::getSummary, kw));
        }
        if (StringUtils.hasText(category) && !"全部".equals(category.trim())) {
            wrapper.eq(Dream::getCategory, category.trim());
        }
        if (status != null && (status == 0 || status == 1)) {
            wrapper.eq(Dream::getStatus, status);
        }
        wrapper.orderByDesc(Dream::getCreateTime);
        IPage<Dream> result = dreamMapper.selectPage(new Page<>(p, s), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Dream d : result.getRecords()) {
            list.add(dreamMap(d));
        }
        return toPageMap(new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Override
    public Map<String, Object> saveDream(AdminDreamDTO dto) {
        if (dto == null) {
            throw new BizException("缺少梦境内容");
        }
        String title = trim(dto.getTitle());
        if (title == null || title.length() < 2 || title.length() > 100) {
            throw new BizException("标题需 2-100 个字符");
        }
        String content = trim(dto.getContent());
        if (!StringUtils.hasText(content)) {
            throw new BizException("正文不能为空");
        }
        if (content.length() > 5000) {
            throw new BizException("正文不能超过 5000 字");
        }
        Dream dream;
        if (dto.getId() != null) {
            dream = dreamMapper.selectById(dto.getId());
            if (dream == null) {
                throw new BizException("梦境不存在或已删除");
            }
        } else {
            dream = new Dream();
            dream.setUserId(0L);                    // 后台创建，无普通用户归属
            dream.setAuthorName("管理员");
            dream.setViews(0);
            dream.setLikes(0);
            dream.setCreateTime(LocalDateTime.now());
        }
        dream.setTitle(title);
        dream.setSummary(StringUtils.hasText(dto.getSummary())
                ? dto.getSummary().trim() : content.substring(0, Math.min(60, content.length())));
        dream.setContent(content);
        dream.setCoverUrl(trim(dto.getCoverUrl()));
        dream.setCategory(StringUtils.hasText(dto.getCategory()) ? dto.getCategory().trim() : "随笔");
        dream.setTags(normalizeTags(dto.getTags()));
        dream.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        dream.setUpdateTime(LocalDateTime.now());
        if (dream.getId() == null) {
            dreamMapper.insert(dream);
        } else {
            dreamMapper.updateById(dream);
        }
        return dreamMap(dreamMapper.selectById(dream.getId()));
    }

    @Override
    public String deleteDream(Long id) {
        Dream dream = requireDream(id);
        dreamMapper.deleteById(id);
        return dream.getTitle();
    }

    // ==================== 广播剧管理 ====================

    @Override
    public Map<String, Object> pageDramas(Integer page, Integer size, String keyword, String category,
                                          String platform, Integer status) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(Drama::getTitle, kw).or().like(Drama::getSummary, kw));
        }
        if (StringUtils.hasText(category) && !"全部".equals(category.trim())) {
            wrapper.eq(Drama::getCategory, category.trim());
        }
        if (StringUtils.hasText(platform) && !"全部".equals(platform.trim())) {
            wrapper.eq(Drama::getPlatform, platform.trim());
        }
        if (status != null && (status == 0 || status == 1)) {
            wrapper.eq(Drama::getStatus, status);
        }
        wrapper.orderByDesc(Drama::getCreateTime);
        IPage<Drama> result = dramaMapper.selectPage(new Page<>(p, s), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Drama d : result.getRecords()) {
            list.add(dramaMap(d));
        }
        return toPageMap(new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Override
    public Map<String, Object> saveDrama(AdminDramaDTO dto) {
        if (dto == null) {
            throw new BizException("缺少广播剧内容");
        }
        String title = trim(dto.getTitle());
        if (title == null || title.length() < 2 || title.length() > 100) {
            throw new BizException("标题需 2-100 个字符");
        }
        Drama drama;
        if (dto.getId() != null) {
            drama = dramaMapper.selectById(dto.getId());
            if (drama == null) {
                throw new BizException("广播剧不存在或已删除");
            }
        } else {
            drama = new Drama();
            drama.setViews(0);
            drama.setLikes(0);
            drama.setCreateTime(LocalDateTime.now());
        }
        drama.setTitle(title);
        drama.setSummary(trim(dto.getSummary()));
        drama.setCoverUrl(trim(dto.getCoverUrl()));
        drama.setCategory(StringUtils.hasText(dto.getCategory()) ? dto.getCategory().trim() : "治愈");
        drama.setTags(normalizeTags(dto.getTags()));
        drama.setPlatform(trim(dto.getPlatform()));
        drama.setVideoUrl(trim(dto.getVideoUrl()));
        drama.setAudioUrl(trim(dto.getAudioUrl()));
        drama.setPosterUrl(trim(dto.getPosterUrl()));
        drama.setDuration(trim(dto.getDuration()));
        drama.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        drama.setUpdateTime(LocalDateTime.now());
        if (drama.getId() == null) {
            dramaMapper.insert(drama);
        } else {
            dramaMapper.updateById(drama);
        }
        return dramaMap(dramaMapper.selectById(drama.getId()));
    }

    @Override
    public String deleteDrama(Long id) {
        if (id == null) {
            throw new BizException("缺少广播剧ID");
        }
        Drama drama = dramaMapper.selectById(id);
        if (drama == null) {
            throw new BizException("广播剧不存在或已删除");
        }
        dramaMapper.deleteById(id);
        return drama.getTitle();
    }

    // ==================== 分类管理 ====================

    @Override
    public List<AdminCategoryVO> categories(String type) {
        String t = normalizeType(type);
        List<AdminCategoryVO> list = new ArrayList<>();
        if ("dream".equals(t)) {
            for (DreamCategory c : dreamCategoryMapper.selectList(new LambdaQueryWrapper<DreamCategory>()
                    .orderByAsc(DreamCategory::getSortNo, DreamCategory::getId))) {
                list.add(new AdminCategoryVO(c.getId(), c.getCode(), c.getName(), c.getIcon(),
                        c.getSortNo(), c.getStatus()));
            }
        } else {
            for (DramaCategory c : dramaCategoryMapper.selectList(new LambdaQueryWrapper<DramaCategory>()
                    .orderByAsc(DramaCategory::getSortNo, DramaCategory::getId))) {
                list.add(new AdminCategoryVO(c.getId(), c.getCode(), c.getName(), c.getIcon(),
                        c.getSortNo(), c.getStatus()));
            }
        }
        return list;
    }

    @Override
    public AdminCategoryVO saveCategory(AdminCategoryDTO dto) {
        if (dto == null) {
            throw new BizException("缺少分类内容");
        }
        String t = normalizeType(dto.getType());
        String name = trim(dto.getName());
        if (name == null || name.isEmpty()) {
            throw new BizException("请填写分类名称");
        }
        boolean isDream = "dream".equals(t);
        String code = trim(dto.getCode());
        if (code == null || code.isEmpty()) {
            code = t + "_cat_" + System.currentTimeMillis();
        }
        // code 同表内唯一（编辑时排除自身）
        long dup;
        if (isDream) {
            dup = dreamCategoryMapper.selectCount(new LambdaQueryWrapper<DreamCategory>()
                    .eq(DreamCategory::getCode, code)
                    .ne(dto.getId() != null, DreamCategory::getId, dto.getId()));
        } else {
            dup = dramaCategoryMapper.selectCount(new LambdaQueryWrapper<DramaCategory>()
                    .eq(DramaCategory::getCode, code)
                    .ne(dto.getId() != null, DramaCategory::getId, dto.getId()));
        }
        if (dup > 0) {
            throw new BizException("分类编码「" + code + "」已存在");
        }
        if (isDream) {
            DreamCategory c;
            if (dto.getId() != null) {
                c = dreamCategoryMapper.selectById(dto.getId());
                if (c == null) {
                    throw new BizException("分类不存在或已删除");
                }
            } else {
                c = new DreamCategory();
                c.setCreateTime(LocalDateTime.now());
            }
            c.setCode(code);
            c.setName(name);
            c.setIcon(trim(dto.getIcon()));
            c.setSortNo(dto.getSortNo() == null ? 99 : dto.getSortNo());
            c.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
            c.setUpdateTime(LocalDateTime.now());
            if (c.getId() == null) {
                dreamCategoryMapper.insert(c);
            } else {
                dreamCategoryMapper.updateById(c);
            }
            return new AdminCategoryVO(c.getId(), c.getCode(), c.getName(), c.getIcon(), c.getSortNo(), c.getStatus());
        }
        DramaCategory c;
        if (dto.getId() != null) {
            c = dramaCategoryMapper.selectById(dto.getId());
            if (c == null) {
                throw new BizException("分类不存在或已删除");
            }
        } else {
            c = new DramaCategory();
            c.setCreateTime(LocalDateTime.now());
        }
        c.setCode(code);
        c.setName(name);
        c.setIcon(trim(dto.getIcon()));
        c.setSortNo(dto.getSortNo() == null ? 99 : dto.getSortNo());
        c.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        c.setUpdateTime(LocalDateTime.now());
        if (c.getId() == null) {
            dramaCategoryMapper.insert(c);
        } else {
            dramaCategoryMapper.updateById(c);
        }
        return new AdminCategoryVO(c.getId(), c.getCode(), c.getName(), c.getIcon(), c.getSortNo(), c.getStatus());
    }

    @Override
    public String deleteCategory(Long id, String type) {
        if (id == null) {
            throw new BizException("缺少分类ID");
        }
        String t = normalizeType(type);
        if ("dream".equals(t)) {
            DreamCategory c = dreamCategoryMapper.selectById(id);
            if (c == null) {
                throw new BizException("分类不存在或已删除");
            }
            long used = dreamMapper.selectCount(new LambdaQueryWrapper<Dream>()
                    .eq(Dream::getCategory, c.getName()));
            if (used > 0) {
                throw new BizException("该分类下还有 " + used + " 条梦境，先移走内容再删除");
            }
            dreamCategoryMapper.deleteById(id);
            return c.getName();
        }
        DramaCategory c = dramaCategoryMapper.selectById(id);
        if (c == null) {
            throw new BizException("分类不存在或已删除");
        }
        long used = dramaMapper.selectCount(new LambdaQueryWrapper<Drama>()
                .eq(Drama::getCategory, c.getName()));
        if (used > 0) {
            throw new BizException("该分类下还有 " + used + " 部广播剧，先移走内容再删除");
        }
        dramaCategoryMapper.deleteById(id);
        return c.getName();
    }

    // ==================== 用户管理 ====================

    @Override
    public Map<String, Object> pageUsers(Integer page, Integer size, String keyword) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        long total = userMapper.selectAdminCount(kw);
        List<Map<String, Object>> list =
                userMapper.selectAdminPage(kw, s, (long) (p - 1) * s);
        // 自定义 @Select 不受 @TableLogic 过滤，这里有意返回已注销用户（前端打「已注销」徽标）
        return toPageMap(new PageResult<>(list, total, p, s));
    }

    @Override
    public String deleteUser(Long id) {
        if (id == null) {
            throw new BizException("缺少用户ID");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在或已注销");
        }
        userMapper.deleteById(id);   // 逻辑删除：列表仍能看到（已注销徽标），登录会被密码校验挡下
        return user.getUsername();
    }

    // ==================== 搜索日志 / 互动数据 ====================

    @Override
    public Map<String, Object> pageSearchLogs(Integer page, Integer size, String keyword) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        LambdaQueryWrapper<DreamSearchLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(DreamSearchLog::getKeyword, keyword.trim());
        }
        wrapper.orderByDesc(DreamSearchLog::getCreateTime);
        IPage<DreamSearchLog> result = searchLogMapper.selectPage(new Page<>(p, s), wrapper);
        List<Map<String, Object>> list = new ArrayList<>();
        for (DreamSearchLog logRow : result.getRecords()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", logRow.getId());
            item.put("keyword", logRow.getKeyword());
            item.put("userId", logRow.getUserId());
            item.put("resultCount", logRow.getResultCount());
            item.put("createTime", logRow.getCreateTime() == null ? "" : FMT.format(logRow.getCreateTime()));
            list.add(item);
        }
        return toPageMap(new PageResult<>(list, result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Override
    public void clearSearchLogs() {
        searchLogMapper.delete(null);   // 物理清空（日志表无逻辑删除需求）
    }

    @Override
    public Map<String, Object> pageLikes(Integer page, Integer size) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        long total = likeMapper.selectAdminCount();
        List<Map<String, Object>> list = likeMapper.selectAdminPage(s, (long) (p - 1) * s);
        return toPageMap(new PageResult<>(list, total, p, s));
    }

    @Override
    public Map<String, Object> pageFavorites(Integer page, Integer size) {
        int p = normalizePage(page);
        int s = normalizeSize(size);
        long total = favoriteMapper.selectAdminCount();
        List<Map<String, Object>> list = favoriteMapper.selectAdminPage(s, (long) (p - 1) * s);
        return toPageMap(new PageResult<>(list, total, p, s));
    }

    // ==================== 内部工具 ====================

    /** 当前请求的管理员身份（JwtInterceptor 已校验 role=admin 并写入 UserContext） */
    private static Long requireAdminId() {
        Long id = com.fenglin.springboottest.common.UserContext.getUserId();
        if (id == null) {
            throw new BizException("管理员登录状态异常，请重新登录");
        }
        return id;
    }

    private Dream requireDream(Long id) {
        if (id == null) {
            throw new BizException("缺少梦境ID");
        }
        Dream dream = dreamMapper.selectById(id);
        if (dream == null) {
            throw new BizException("梦境不存在或已删除");
        }
        return dream;
    }

    private Map<String, Object> dreamMap(Dream d) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", d.getId());
        item.put("userId", d.getUserId());
        item.put("authorName", d.getAuthorName());
        item.put("title", d.getTitle());
        item.put("summary", d.getSummary());
        item.put("content", d.getContent());
        item.put("coverUrl", d.getCoverUrl());
        item.put("category", d.getCategory());
        item.put("tags", d.getTags());
        item.put("status", d.getStatus());
        item.put("views", d.getViews());
        item.put("likes", d.getLikes());
        item.put("createTime", d.getCreateTime() == null ? "" : FMT.format(d.getCreateTime()));
        return item;
    }

    private Map<String, Object> dramaMap(Drama d) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", d.getId());
        item.put("title", d.getTitle());
        item.put("summary", d.getSummary());
        item.put("coverUrl", d.getCoverUrl());
        item.put("category", d.getCategory());
        item.put("tags", d.getTags());
        item.put("platform", d.getPlatform());
        item.put("videoUrl", d.getVideoUrl());
        item.put("audioUrl", d.getAudioUrl());
        item.put("posterUrl", d.getPosterUrl());
        item.put("duration", d.getDuration());
        item.put("status", d.getStatus());
        item.put("views", d.getViews());
        item.put("likes", d.getLikes());
        item.put("createTime", d.getCreateTime() == null ? "" : FMT.format(d.getCreateTime()));
        return item;
    }

    private Map<String, Object> toPageMap(PageResult<Map<String, Object>> pr) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", pr.getList());
        data.put("total", pr.getTotal());
        data.put("page", pr.getPage());
        data.put("size", pr.getSize());
        data.put("pages", pr.getPages());
        data.put("hasMore", pr.isHasMore());
        return data;
    }

    private static String normalizeType(String type) {
        String t = trim(type);
        if (!"dream".equals(t) && !"drama".equals(t)) {
            throw new BizException("非法的分类类型：" + t);
        }
        return t;
    }

    private static String normalizeTags(String tags) {
        if (!StringUtils.hasText(tags)) {
            return null;
        }
        // 去重、去空、逗号分隔，最多 8 个
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String tag : tags.split("[,，]")) {
            String t = tag.trim();
            if (!t.isEmpty()) {
                set.add(t);
            }
            if (set.size() >= 8) {
                break;
            }
        }
        return set.isEmpty() ? null : String.join(",", set);
    }

    private static int normalizePage(Integer page) {
        return (page == null || page < 1) ? 1 : page;
    }

    private static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return 10;
        }
        return Math.min(size, MAX_SIZE);
    }

    private static String trim(String s) {
        return (s == null) ? null : s.trim();
    }
}
