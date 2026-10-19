package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.fenglin.springboottest.service.DreamService;
import com.fenglin.springboottest.vo.CategoryVO;
import com.fenglin.springboottest.vo.DreamVO;
import com.fenglin.springboottest.vo.HomeOverviewVO;
import com.fenglin.springboottest.vo.HomeStatsVO;
import com.fenglin.springboottest.vo.HotKeywordVO;
import com.fenglin.springboottest.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DreamServiceImpl implements DreamService {

    private static final Logger log = LoggerFactory.getLogger(DreamServiceImpl.class);

    /** 允许的分类白名单（与 t_dream_category 种子数据保持一致） */
    private static final List<String> CATEGORIES = Arrays.asList("美梦", "噩梦", "灵感", "随笔");

    /** 兜底热门搜索词：搜索日志还没积累时使用 */
    private static final List<String> DEFAULT_HOT_KEYWORDS =
            Arrays.asList("云朵", "星星", "月亮", "糖果", "鲸鱼", "温柔", "黄昏", "花园");

    /** 单次返回上限，防止误传超大 size 拖垮数据库 */
    private static final int MAX_SIZE = 50;

    /** 热门搜索词条数 */
    private static final int HOT_KEYWORD_LIMIT = 8;

    /** 首页推荐位条数（热门 / 最新各取这么多条） */
    private static final int RECOMMEND_LIMIT = 6;

    /** 热门搜索词统计窗口：近 N 天 */
    private static final int HOT_KEYWORD_DAYS = 30;

    private final DreamMapper dreamMapper;
    private final DreamCategoryMapper categoryMapper;
    private final DreamLikeMapper likeMapper;
    private final DreamFavoriteMapper favoriteMapper;
    private final DreamSearchLogMapper searchLogMapper;

    public DreamServiceImpl(DreamMapper dreamMapper,
                            DreamCategoryMapper categoryMapper,
                            DreamLikeMapper likeMapper,
                            DreamFavoriteMapper favoriteMapper,
                            DreamSearchLogMapper searchLogMapper) {
        this.dreamMapper = dreamMapper;
        this.categoryMapper = categoryMapper;
        this.likeMapper = likeMapper;
        this.favoriteMapper = favoriteMapper;
        this.searchLogMapper = searchLogMapper;
    }

    // ==================== 首页展示 ====================

    @Override
    public PageResult<DreamVO> pageDreams(DreamQuery query) {
        DreamQuery q = (query == null) ? new DreamQuery() : query;
        int page = (q.getPage() == null || q.getPage() < 1) ? 1 : q.getPage();
        int size = normalizeSize(q.getSize());

        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<>();
        // 只展示已发布的内容（deleted 由逻辑删除自动过滤）
        wrapper.eq(Dream::getStatus, 1);
        applyKeyword(wrapper, q.getKeyword());
        applyCategory(wrapper, q.getCategory());
        applySort(wrapper, q.getSort());
        // 只看某位作者的内容（「我的梦境」走 myDreams，那里会强制覆盖成登录用户）
        if (q.getAuthorId() != null && q.getAuthorId() > 0) {
            wrapper.eq(Dream::getUserId, q.getAuthorId());
        }

        IPage<Dream> pageReq = dreamMapper.selectPage(new Page<>(page, size), wrapper);
        List<DreamVO> list = toVOList(pageReq.getRecords(), viewerId(q.getUserId()), false);
        return new PageResult<>(list, pageReq.getTotal(), pageReq.getCurrent(), pageReq.getSize());
    }

    @Override
    public PageResult<DreamVO> search(DreamQuery query) {
        PageResult<DreamVO> result = pageDreams(query);
        String keyword = (query == null) ? null : trim(query.getKeyword());
        if (StringUtils.hasText(keyword)) {
            recordSearchLog(keyword, viewerId(query.getUserId()), (int) result.getTotal());
        }
        return result;
    }

    @Override
    public HomeOverviewVO overview(Long userId) {
        Long viewerId = viewerId(userId);
        HomeOverviewVO vo = new HomeOverviewVO();
        vo.setStats(buildStats());
        vo.setCategories(categories());
        vo.setHotKeywords(hotKeywords());
        vo.setHotDreams(topDreams("views", viewerId));
        vo.setLatestDreams(topDreams("newest", viewerId));
        return vo;
    }

    @Override
    public List<CategoryVO> categories() {
        // 各分类的已发布内容数量
        Map<String, Long> countMap = new HashMap<>();
        List<Map<String, Object>> rows = dreamMapper.selectMaps(new QueryWrapper<Dream>()
                .select("category AS category", "COUNT(*) AS cnt")
                .eq("status", 1)
                .eq("deleted", 0)
                .groupBy("category"));
        for (Map<String, Object> row : rows) {
            Object name = row.get("category");
            if (name != null) {
                countMap.put(name.toString(), toLong(row.get("cnt")));
            }
        }

        List<DreamCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<DreamCategory>()
                .eq(DreamCategory::getStatus, 1)
                .orderByAsc(DreamCategory::getSortNo, DreamCategory::getId));

        List<CategoryVO> list = new ArrayList<>();
        if (categories.isEmpty()) {
            // 分类表为空时按白名单兜底，保证首页分类导航不空
            for (String name : CATEGORIES) {
                list.add(new CategoryVO(null, codeOf(name), name, codeOf(name), countMap.getOrDefault(name, 0L)));
            }
            return list;
        }
        for (DreamCategory c : categories) {
            list.add(new CategoryVO(c.getId(), c.getCode(), c.getName(), c.getIcon(),
                    countMap.getOrDefault(c.getName(), 0L)));
        }
        return list;
    }

    @Override
    public List<HotKeywordVO> hotKeywords() {
        List<Map<String, Object>> rows = searchLogMapper.selectMaps(new QueryWrapper<DreamSearchLog>()
                .select("keyword AS keyword", "COUNT(*) AS cnt")
                .ge("create_time", LocalDateTime.now().minusDays(HOT_KEYWORD_DAYS))
                .groupBy("keyword")
                .orderByDesc("cnt")
                .last("LIMIT " + HOT_KEYWORD_LIMIT));

        List<HotKeywordVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Object kw = row.get("keyword");
            if (kw != null && StringUtils.hasText(kw.toString())) {
                list.add(new HotKeywordVO(kw.toString(), toLong(row.get("cnt"))));
            }
        }
        if (list.isEmpty()) {
            // 搜索日志为空：返回默认推荐词，保证首页热门搜索区有内容
            for (String kw : DEFAULT_HOT_KEYWORDS) {
                list.add(new HotKeywordVO(kw, 0L));
            }
        }
        return list;
    }

    // ==================== 详情 / 互动 / 发布 ====================

    @Override
    public DreamVO detail(Long id, Long userId) {
        if (id == null) {
            throw new BizException("缺少梦境ID");
        }
        Dream dream = dreamMapper.selectById(id);
        if (dream == null || dream.getStatus() == null || dream.getStatus() != 1) {
            throw new BizException("梦境不存在或已下架");
        }
        // 浏览量原子自增，避免并发覆盖
        dreamMapper.update(null, new LambdaUpdateWrapper<Dream>()
                .eq(Dream::getId, id)
                .setSql("views = views + 1"));

        Dream latest = dreamMapper.selectById(id);
        Long viewerId = viewerId(userId);
        DreamVO vo = DreamVO.of(latest);
        vo.setCategoryCode(categoryCodeMap().get(vo.getCategory()));
        vo.setLiked(isLiked(id, viewerId));
        vo.setFavorited(isFavorited(id, viewerId));
        return vo;
    }

    @Override
    public Map<String, Object> toggleLike(DreamActionDTO dto) {
        Long dreamId = requireDreamId(dto);
        Long userId = requireLogin("请先登录后再点赞");
        Dream dream = requirePublished(dreamId);

        DreamLike exist = likeMapper.selectOne(new LambdaQueryWrapper<DreamLike>()
                .eq(DreamLike::getDreamId, dreamId)
                .eq(DreamLike::getUserId, userId)
                .last("limit 1"));

        String action = trim(dto.getAction());
        boolean wantLike;
        if ("like".equalsIgnoreCase(action)) {
            wantLike = true;
        } else if ("unlike".equalsIgnoreCase(action) || "cancel".equalsIgnoreCase(action)) {
            wantLike = false;
        } else {
            wantLike = (exist == null);   // toggle
        }

        boolean liked = (exist != null);
        if (wantLike && exist == null) {
            DreamLike record = new DreamLike();
            record.setDreamId(dreamId);
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            try {
                likeMapper.insert(record);
            } catch (DuplicateKeyException e) {
                // 并发重复点赞：唯一键挡下即可，不重复加计数
                log.debug("重复点赞，已忽略：dream={} user={}", dreamId, userId);
                return likeResult(dreamId, true);
            }
            dreamMapper.update(null, new LambdaUpdateWrapper<Dream>()
                    .eq(Dream::getId, dreamId)
                    .setSql("likes = likes + 1"));
            liked = true;
        } else if (!wantLike && exist != null) {
            likeMapper.deleteById(exist.getId());
            dreamMapper.update(null, new LambdaUpdateWrapper<Dream>()
                    .eq(Dream::getId, dreamId)
                    .setSql("likes = GREATEST(likes - 1, 0)"));
            liked = false;
        }
        log.info("点赞操作：dream={} user={} 结果 liked={}（作者 {}）", dreamId, userId, liked, dream.getAuthorName());
        return likeResult(dreamId, liked);
    }

    @Override
    public Map<String, Object> toggleFavorite(DreamActionDTO dto) {
        Long dreamId = requireDreamId(dto);
        Long userId = requireLogin("请先登录后再收藏");
        requirePublished(dreamId);

        DreamFavorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<DreamFavorite>()
                .eq(DreamFavorite::getDreamId, dreamId)
                .eq(DreamFavorite::getUserId, userId)
                .last("limit 1"));

        String action = trim(dto.getAction());
        boolean wantFavorite;
        if ("favorite".equalsIgnoreCase(action) || "add".equalsIgnoreCase(action)) {
            wantFavorite = true;
        } else if ("unfavorite".equalsIgnoreCase(action) || "cancel".equalsIgnoreCase(action)) {
            wantFavorite = false;
        } else {
            wantFavorite = (exist == null);   // toggle
        }

        boolean favorited = (exist != null);
        if (wantFavorite && exist == null) {
            DreamFavorite record = new DreamFavorite();
            record.setDreamId(dreamId);
            record.setUserId(userId);
            record.setCreateTime(LocalDateTime.now());
            try {
                favoriteMapper.insert(record);
            } catch (DuplicateKeyException e) {
                log.debug("重复收藏，已忽略：dream={} user={}", dreamId, userId);
                return favoriteResult(dreamId, true);
            }
            favorited = true;
        } else if (!wantFavorite && exist != null) {
            favoriteMapper.deleteById(exist.getId());
            favorited = false;
        }
        return favoriteResult(dreamId, favorited);
    }

    @Override
    public PageResult<DreamVO> myFavorites(DreamQuery query) {
        Long userId = requireLogin("请先登录后查看收藏");
        DreamQuery q = (query == null) ? new DreamQuery() : query;
        int page = (q.getPage() == null || q.getPage() < 1) ? 1 : q.getPage();
        int size = normalizeSize(q.getSize());

        // 先按收藏时间倒序分页取收藏记录，再按 id 顺序组回梦境内容
        IPage<DreamFavorite> favPage = favoriteMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<DreamFavorite>()
                        .eq(DreamFavorite::getUserId, userId)
                        .orderByDesc(DreamFavorite::getCreateTime, DreamFavorite::getId));

        if (favPage.getRecords().isEmpty()) {
            return PageResult.empty(favPage.getCurrent(), favPage.getSize());
        }
        List<Long> ids = favPage.getRecords().stream()
                .map(DreamFavorite::getDreamId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, Dream> byId = new HashMap<>();
        for (Dream d : dreamMapper.selectList(new LambdaQueryWrapper<Dream>()
                .in(Dream::getId, ids)
                .eq(Dream::getStatus, 1))) {
            byId.put(d.getId(), d);
        }

        Map<String, String> codeMap = categoryCodeMap();
        Set<Long> likedIds = likedDreamIds(ids, userId);
        List<DreamVO> list = new ArrayList<>(ids.size());
        for (Long id : ids) {
            Dream d = byId.get(id);
            if (d == null) {
                continue;   // 已下架/被删的收藏跳过
            }
            DreamVO vo = DreamVO.of(d).withoutContent();
            vo.setCategoryCode(codeMap.get(vo.getCategory()));
            vo.setLiked(likedIds.contains(id));
            vo.setFavorited(true);
            list.add(vo);
        }
        return new PageResult<>(list, favPage.getTotal(), favPage.getCurrent(), favPage.getSize());
    }

    @Override
    public Dream publish(DreamPublishDTO dto) {
        Long userId = requireLogin("请先登录后再发布梦境");
        if (dto == null) {
            throw new BizException("请填写梦境标题");
        }
        Dream dream = new Dream();
        // 作者身份一律取登录态，不信任请求体（避免伪造署名）
        dream.setUserId(userId);
        String authorName = trim(UserContext.getUsername());
        dream.setAuthorName(StringUtils.hasText(authorName) ? authorName : "匿名梦友");
        fillEditableFields(dream, dto.getTitle(), dto.getSummary(), dto.getContent(),
                dto.getCategory(), dto.getTags(), dto.getCoverUrl());

        dream.setStatus(1);          // 默认直接发布
        dream.setViews(0);
        dream.setLikes(0);
        dream.setCreateTime(LocalDateTime.now());
        dream.setUpdateTime(LocalDateTime.now());

        dreamMapper.insert(dream);
        log.info("新梦境发布成功：{}（作者 {}，分类 {}，user={}）",
                dream.getTitle(), dream.getAuthorName(), dream.getCategory(), userId);
        return dream;
    }

    @Override
    public PageResult<DreamVO> myDreams(DreamQuery query) {
        Long userId = requireLogin("请先登录后查看我的梦境");
        DreamQuery q = (query == null) ? new DreamQuery() : query;
        int page = (q.getPage() == null || q.getPage() < 1) ? 1 : q.getPage();
        int size = normalizeSize(q.getSize());

        // 强制只看自己：外部传入的 authorId 一律忽略
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<Dream>()
                .eq(Dream::getStatus, 1)
                .eq(Dream::getUserId, userId);
        applyKeyword(wrapper, q.getKeyword());
        applySort(wrapper, q.getSort());

        IPage<Dream> pageReq = dreamMapper.selectPage(new Page<>(page, size), wrapper);
        // 「我的梦境」需要正文，方便前端直接打开编辑（也避免为了编辑去调详情接口而白涨浏览量）
        return new PageResult<>(toVOList(pageReq.getRecords(), userId, true),
                pageReq.getTotal(), pageReq.getCurrent(), pageReq.getSize());
    }

    @Override
    public Dream update(DreamUpdateDTO dto) {
        Long userId = requireLogin("请先登录后再编辑梦境");
        if (dto == null || dto.getId() == null) {
            throw new BizException("缺少梦境ID");
        }
        Dream dream = dreamMapper.selectById(dto.getId());
        if (dream == null) {
            throw new BizException("梦境不存在或已删除");
        }
        if (!Objects.equals(dream.getUserId(), userId)) {
            throw new BizException("只能编辑自己发布的梦境");
        }

        fillEditableFields(dream, dto.getTitle(), dto.getSummary(), dto.getContent(),
                dto.getCategory(), dto.getTags(), dto.getCoverUrl());
        dream.setUpdateTime(LocalDateTime.now());
        dreamMapper.updateById(dream);
        log.info("梦境已编辑：id={} title={} user={}", dream.getId(), dream.getTitle(), userId);
        return dream;
    }

    @Override
    public String deleteDream(Long id) {
        Long userId = requireLogin("请先登录后再删除梦境");
        if (id == null) {
            throw new BizException("缺少梦境ID");
        }
        Dream dream = dreamMapper.selectById(id);
        if (dream == null) {
            throw new BizException("梦境不存在或已删除");
        }
        if (!Objects.equals(dream.getUserId(), userId)) {
            throw new BizException("只能删除自己发布的梦境");
        }
        // @TableLogic → 逻辑删除，不物理删行，避免误删无法追溯
        dreamMapper.deleteById(id);
        log.info("梦境已删除：id={} title={} user={}", id, dream.getTitle(), userId);
        return dream.getTitle();
    }

    @Override
    public Map<String, Object> profile() {
        Long userId = requireLogin("请先登录后查看个人中心");

        List<Object> idObjs = dreamMapper.selectObjs(new QueryWrapper<Dream>()
                .select("id")
                .eq("user_id", userId)
                .eq("deleted", 0));
        List<Long> myDreamIds = new ArrayList<>();
        for (Object o : idObjs) {
            if (o != null) {
                myDreamIds.add(toLong(o));
            }
        }

        long favoriteCount = 0L;
        if (!myDreamIds.isEmpty()) {
            Long c = favoriteMapper.selectCount(new LambdaQueryWrapper<DreamFavorite>()
                    .in(DreamFavorite::getDreamId, myDreamIds));
            favoriteCount = (c == null) ? 0L : c;
        }

        long viewSum = 0L;
        long likeSum = 0L;
        List<Map<String, Object>> rows = dreamMapper.selectMaps(new QueryWrapper<Dream>()
                .select("IFNULL(SUM(views), 0) AS view_sum", "IFNULL(SUM(likes), 0) AS like_sum")
                .eq("user_id", userId)
                .eq("deleted", 0));
        if (!rows.isEmpty()) {
            viewSum = toLong(rows.get(0).get("view_sum"));
            likeSum = toLong(rows.get(0).get("like_sum"));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", userId);
        data.put("username", UserContext.getUsername());
        data.put("dreamCount", myDreamIds.size());
        data.put("viewCount", viewSum);
        data.put("likeCount", likeSum);
        data.put("favoriteCount", favoriteCount);
        return data;
    }

    /**
     * 发布 / 编辑共用的字段校验与归一：
     * 标题必填 2-100 字；正文必填（编辑时允许与原来一致）；摘要空则取正文前 60 字；非法分类回落随笔；标签去重最多 8 个。
     */
    private void fillEditableFields(Dream dream, String rawTitle, String rawSummary, String rawContent,
                                    String rawCategory, String rawTags, String rawCoverUrl) {
        if (!StringUtils.hasText(rawTitle)) {
            throw new BizException("请填写梦境标题");
        }
        String title = rawTitle.trim();
        if (title.length() < 2 || title.length() > 100) {
            throw new BizException("标题长度需在 2-100 字之间");
        }
        String summary = rawSummary == null ? "" : rawSummary.trim();
        if (summary.length() > 255) {
            throw new BizException("摘要不能超过 255 字");
        }
        String content = rawContent == null ? "" : rawContent.trim();
        if (!StringUtils.hasText(content)) {
            throw new BizException("请填写梦境正文");
        }
        if (content.length() > 5000) {
            throw new BizException("正文不能超过 5000 字");
        }

        dream.setTitle(title);
        dream.setSummary(summary.isEmpty() ? clip(content, 60) : summary);
        dream.setContent(content);
        dream.setCoverUrl(trim(rawCoverUrl));

        String category = trim(rawCategory);
        dream.setCategory(CATEGORIES.contains(category) ? category : "随笔");
        dream.setTags(normalizeTags(rawTags));
    }

    // ==================== 内部工具 ====================

    /** 登录用户优先，未登录时回落到请求体里的 userId（仅用于读接口展示互动状态） */
    private Long viewerId(Long fallback) {
        Long uid = UserContext.getUserId();
        return uid != null ? uid : fallback;
    }

    private Long requireLogin(String message) {
        Long uid = UserContext.getUserId();
        if (uid == null) {
            throw new AuthException(message);
        }
        return uid;
    }

    private Long requireDreamId(DreamActionDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BizException("缺少梦境ID");
        }
        return dto.getId();
    }

    private Dream requirePublished(Long dreamId) {
        Dream dream = dreamMapper.selectById(dreamId);
        if (dream == null || dream.getStatus() == null || dream.getStatus() != 1) {
            throw new BizException("梦境不存在或已下架");
        }
        return dream;
    }

    private int normalizeSize(Integer size) {
        int value = (size == null || size < 1) ? 10 : size;
        return Math.min(value, MAX_SIZE);
    }

    /** 关键词：标题 / 摘要 / 正文 / 标签 / 作者 任一包含即可 */
    private void applyKeyword(LambdaQueryWrapper<Dream> wrapper, String rawKeyword) {
        String keyword = trim(rawKeyword);
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        // 模糊查询通配符转义，避免用户输入的 % _ 造成全表匹配
        String like = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        wrapper.and(w -> w
                .like(Dream::getTitle, like)
                .or().like(Dream::getSummary, like)
                .or().like(Dream::getContent, like)
                .or().like(Dream::getTags, like)
                .or().like(Dream::getAuthorName, like));
    }

    /** 分类：空 / "全部" 不限；同时兼容分类名（美梦）与分类编码（beauty） */
    private void applyCategory(LambdaQueryWrapper<Dream> wrapper, String rawCategory) {
        String category = trim(rawCategory);
        if (!StringUtils.hasText(category) || "全部".equals(category) || "all".equalsIgnoreCase(category)) {
            return;
        }
        String name = category;
        if (!CATEGORIES.contains(category)) {
            // 传的是编码，换成分类名
            for (DreamCategory c : categoryMapper.selectList(new LambdaQueryWrapper<DreamCategory>()
                    .eq(DreamCategory::getCode, category))) {
                name = c.getName();
            }
        }
        wrapper.eq(Dream::getCategory, name);
    }

    /** 排序：hot 按浏览量倒序；newest（默认）按发布时间倒序 */
    private void applySort(LambdaQueryWrapper<Dream> wrapper, String rawSort) {
        if ("hot".equalsIgnoreCase(trim(rawSort))) {
            wrapper.orderByDesc(Dream::getViews, Dream::getCreateTime, Dream::getId);
        } else {
            wrapper.orderByDesc(Dream::getCreateTime, Dream::getId);
        }
    }

    /** 首页推荐位：按浏览量或时间取前 N 条 */
    private List<DreamVO> topDreams(String sort, Long userId) {
        LambdaQueryWrapper<Dream> wrapper = new LambdaQueryWrapper<Dream>()
                .eq(Dream::getStatus, 1);
        if ("views".equals(sort)) {
            wrapper.orderByDesc(Dream::getViews, Dream::getCreateTime);
        } else {
            wrapper.orderByDesc(Dream::getCreateTime, Dream::getId);
        }
        wrapper.last("limit " + RECOMMEND_LIMIT);
        return toVOList(dreamMapper.selectList(wrapper), userId, false);
    }

    /** 顶部统计：内容数 / 总浏览 / 总点赞 / 作者数 */
    private HomeStatsVO buildStats() {
        List<Map<String, Object>> rows = dreamMapper.selectMaps(new QueryWrapper<Dream>()
                .select("COUNT(*) AS cnt",
                        "IFNULL(SUM(views), 0) AS view_sum",
                        "IFNULL(SUM(likes), 0) AS like_sum",
                        "COUNT(DISTINCT author_name) AS author_cnt")
                .eq("status", 1)
                .eq("deleted", 0));
        if (rows.isEmpty()) {
            return new HomeStatsVO();
        }
        Map<String, Object> row = rows.get(0);
        return new HomeStatsVO(
                toLong(row.get("cnt")),
                toLong(row.get("view_sum")),
                toLong(row.get("like_sum")),
                toLong(row.get("author_cnt")));
    }

    /** 实体集合 → VO 集合（批量填充分类编码、liked、favorited，避免 N+1 查询） */
    private List<DreamVO> toVOList(List<Dream> dreams, Long userId, boolean withContent) {
        if (dreams == null || dreams.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, String> codeMap = categoryCodeMap();
        List<Long> ids = dreams.stream().map(Dream::getId).filter(Objects::nonNull).collect(Collectors.toList());
        Set<Long> likedIds = likedDreamIds(ids, userId);
        Set<Long> favoriteIds = favoriteDreamIds(ids, userId);

        List<DreamVO> list = new ArrayList<>(dreams.size());
        for (Dream d : dreams) {
            DreamVO vo = DreamVO.of(d).withoutContent();
            vo.setCategoryCode(codeMap.get(vo.getCategory()));
            vo.setLiked(likedIds.contains(vo.getId()));
            vo.setFavorited(favoriteIds.contains(vo.getId()));
            if (withContent) {
                vo.setContent(d.getContent());
            }
            list.add(vo);
        }
        return list;
    }

    /** 一次查出当前用户在这批内容里点过赞的 ID 集合 */
    private Set<Long> likedDreamIds(List<Long> dreamIds, Long userId) {
        if (userId == null || userId <= 0 || dreamIds == null || dreamIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<DreamLike> likes = likeMapper.selectList(new LambdaQueryWrapper<DreamLike>()
                .eq(DreamLike::getUserId, userId)
                .in(DreamLike::getDreamId, dreamIds));
        Set<Long> set = new HashSet<>();
        for (DreamLike l : likes) {
            set.add(l.getDreamId());
        }
        return set;
    }

    /** 一次查出当前用户在这批内容里收藏过的 ID 集合 */
    private Set<Long> favoriteDreamIds(List<Long> dreamIds, Long userId) {
        if (userId == null || userId <= 0 || dreamIds == null || dreamIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<DreamFavorite> favorites = favoriteMapper.selectList(new LambdaQueryWrapper<DreamFavorite>()
                .eq(DreamFavorite::getUserId, userId)
                .in(DreamFavorite::getDreamId, dreamIds));
        Set<Long> set = new HashSet<>();
        for (DreamFavorite f : favorites) {
            set.add(f.getDreamId());
        }
        return set;
    }

    private boolean isLiked(Long dreamId, Long userId) {
        if (dreamId == null || userId == null || userId <= 0) {
            return false;
        }
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<DreamLike>()
                .eq(DreamLike::getDreamId, dreamId)
                .eq(DreamLike::getUserId, userId));
        return count != null && count > 0;
    }

    private boolean isFavorited(Long dreamId, Long userId) {
        if (dreamId == null || userId == null || userId <= 0) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<DreamFavorite>()
                .eq(DreamFavorite::getDreamId, dreamId)
                .eq(DreamFavorite::getUserId, userId));
        return count != null && count > 0;
    }

    private Map<String, Object> likeResult(Long dreamId, boolean liked) {
        Dream latest = dreamMapper.selectById(dreamId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", dreamId);
        data.put("liked", liked);
        data.put("likes", latest == null || latest.getLikes() == null ? 0 : latest.getLikes());
        return data;
    }

    private Map<String, Object> favoriteResult(Long dreamId, boolean favorited) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", dreamId);
        data.put("favorited", favorited);
        return data;
    }

    /** 分类名 → 分类编码，前端据此配色调性 */
    private Map<String, String> categoryCodeMap() {
        Map<String, String> map = new HashMap<>();
        for (DreamCategory c : categoryMapper.selectList(null)) {
            map.put(c.getName(), c.getCode());
        }
        if (map.isEmpty()) {
            for (String name : CATEGORIES) {
                map.put(name, codeOf(name));
            }
        }
        return map;
    }

    private static String codeOf(String name) {
        switch (name) {
            case "美梦":
                return "beauty";
            case "噩梦":
                return "nightmare";
            case "灵感":
                return "inspiration";
            default:
                return "essay";
        }
    }

    /** 搜索日志：失败不影响搜索主流程 */
    private void recordSearchLog(String keyword, Long userId, int resultCount) {
        try {
            DreamSearchLog logRow = new DreamSearchLog();
            logRow.setKeyword(clip(keyword, 50));
            logRow.setUserId(userId == null ? 0L : userId);
            logRow.setResultCount(resultCount);
            logRow.setCreateTime(LocalDateTime.now());
            searchLogMapper.insert(logRow);
        } catch (Exception e) {
            log.warn("记录搜索日志失败：{}", e.getMessage());
        }
    }

    private static String normalizeTags(String tags) {
        String t = trim(tags);
        if (!StringUtils.hasText(t)) {
            return "";
        }
        return Arrays.stream(t.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(8)
                .collect(Collectors.joining(","));
    }

    private static String clip(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
