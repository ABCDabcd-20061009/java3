package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.DramaDetailDTO;
import com.fenglin.springboottest.dto.DramaQuery;
import com.fenglin.springboottest.entity.Drama;
import com.fenglin.springboottest.entity.DramaCategory;
import com.fenglin.springboottest.mapper.DramaCategoryMapper;
import com.fenglin.springboottest.mapper.DramaMapper;
import com.fenglin.springboottest.service.DramaService;
import com.fenglin.springboottest.vo.CategoryVO;
import com.fenglin.springboottest.vo.DramaVO;
import com.fenglin.springboottest.vo.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 广播剧服务实现：浏览列表（分类/排序/关键词/分页）、详情（浏览量+1）、分类导航。
 * 广播剧为公开内容，浏览/详情/分类均允许匿名访问（无需登录）。
 */
@Service
public class DramaServiceImpl implements DramaService {

    private static final Logger log = LoggerFactory.getLogger(DramaServiceImpl.class);

    /** 兜底分类（与 t_drama_category 种子数据保持一致），分类表为空时也能渲染导航 */
    private static final List<String> CATEGORIES = Arrays.asList("悬疑", "言情", "科幻", "治愈", "古风", "儿童");

    /** 分类名 → 分类编码，用于前端配色调性（兜底，分类表查不到时回落这里） */
    private static final Map<String, String> CODE_OF = new HashMap<>();
    static {
        CODE_OF.put("悬疑", "suspense");
        CODE_OF.put("言情", "romance");
        CODE_OF.put("科幻", "sci-fi");
        CODE_OF.put("治愈", "healing");
        CODE_OF.put("古风", "ancient");
        CODE_OF.put("儿童", "children");
    }

    /** 单次返回上限，防止误传超大 size 拖垮数据库 */
    private static final int MAX_SIZE = 50;

    private final DramaMapper dramaMapper;
    private final DramaCategoryMapper categoryMapper;

    public DramaServiceImpl(DramaMapper dramaMapper, DramaCategoryMapper categoryMapper) {
        this.dramaMapper = dramaMapper;
        this.categoryMapper = categoryMapper;
    }

    // ==================== 列表 / 详情 / 分类 ====================

    @Override
    public PageResult<DramaVO> pageDramas(DramaQuery query) {
        DramaQuery q = (query == null) ? new DramaQuery() : query;
        int page = (q.getPage() == null || q.getPage() < 1) ? 1 : q.getPage();
        int size = normalizeSize(q.getSize());

        LambdaQueryWrapper<Drama> wrapper = new LambdaQueryWrapper<>();
        // 只展示已发布的内容（deleted 由逻辑删除自动过滤）
        wrapper.eq(Drama::getStatus, 1);
        applyKeyword(wrapper, q.getKeyword());
        applyCategory(wrapper, q.getCategory());
        applyPlatform(wrapper, q.getPlatform());
        applySort(wrapper, q.getSort());

        IPage<Drama> pageReq = dramaMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, String> codeMap = categoryCodeMap();
        List<DramaVO> list = pageReq.getRecords().stream().map(d -> {
            DramaVO vo = DramaVO.of(d);
            vo.setCategoryCode(codeMap.getOrDefault(d.getCategory(), "other"));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult<>(list, pageReq.getTotal(), pageReq.getCurrent(), pageReq.getSize());
    }

    @Override
    public DramaVO detail(Long id, Long userId) {
        if (id == null) {
            throw new BizException("缺少广播剧ID");
        }
        Drama drama = dramaMapper.selectById(id);
        if (drama == null || drama.getStatus() == null || drama.getStatus() != 1) {
            throw new BizException("广播剧不存在或已下架");
        }
        // 浏览量原子自增，避免并发覆盖
        dramaMapper.update(null, new LambdaUpdateWrapper<Drama>()
                .eq(Drama::getId, id)
                .setSql("views = views + 1"));

        Drama latest = dramaMapper.selectById(id);
        DramaVO vo = DramaVO.of(latest);
        vo.setCategoryCode(categoryCodeMap().getOrDefault(latest.getCategory(), "other"));
        return vo;
    }

    @Override
    public List<CategoryVO> categories() {
        // 各分类的已发布广播剧数量
        Map<String, Long> countMap = new HashMap<>();
        List<Map<String, Object>> rows = dramaMapper.selectMaps(new QueryWrapper<Drama>()
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

        List<DramaCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<DramaCategory>()
                .eq(DramaCategory::getStatus, 1)
                .orderByAsc(DramaCategory::getSortNo, DramaCategory::getId));

        List<CategoryVO> list = new ArrayList<>();
        if (categories.isEmpty()) {
            // 分类表为空时按白名单兜底，保证分类导航不空
            for (String name : CATEGORIES) {
                list.add(new CategoryVO(null, CODE_OF.getOrDefault(name, "other"), name,
                        CODE_OF.getOrDefault(name, "other"), countMap.getOrDefault(name, 0L)));
            }
            return list;
        }
        for (DramaCategory c : categories) {
            list.add(new CategoryVO(c.getId(), c.getCode(), c.getName(), c.getIcon(),
                    countMap.getOrDefault(c.getName(), 0L)));
        }
        return list;
    }

    // ==================== 内部工具 ====================

    /** 分类名 → 编码，优先查分类字典，查不到回落到内置映射 */
    private Map<String, String> categoryCodeMap() {
        List<DramaCategory> cats = categoryMapper.selectList(new LambdaQueryWrapper<DramaCategory>()
                .eq(DramaCategory::getStatus, 1));
        Map<String, String> map = new HashMap<>();
        for (DramaCategory c : cats) {
            if (c.getName() != null) {
                map.put(c.getName(), c.getCode());
            }
        }
        return map;
    }

    private int normalizeSize(Integer size) {
        int value = (size == null || size < 1) ? 8 : size;
        return Math.min(value, MAX_SIZE);
    }

    /** 关键词：标题 / 简介 / 标签 任一包含即可（含通配符转义，避免 % _ 造成全表匹配） */
    private void applyKeyword(LambdaQueryWrapper<Drama> wrapper, String rawKeyword) {
        String keyword = trim(rawKeyword);
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String like = keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        wrapper.and(w -> w
                .like(Drama::getTitle, like)
                .or().like(Drama::getSummary, like)
                .or().like(Drama::getTags, like));
    }

    /** 分类：空 / "全部" 不限；兼容分类名（悬疑）与编码（suspense） */
    private void applyCategory(LambdaQueryWrapper<Drama> wrapper, String rawCategory) {
        String category = trim(rawCategory);
        if (!StringUtils.hasText(category) || "全部".equals(category) || "all".equalsIgnoreCase(category)) {
            return;
        }
        // 传的是编码则换成分类名再过滤
        if (!CATEGORIES.contains(category)) {
            DramaCategory hit = categoryMapper.selectOne(new LambdaQueryWrapper<DramaCategory>()
                    .eq(DramaCategory::getCode, category)
                    .eq(DramaCategory::getStatus, 1)
                    .last("limit 1"));
            if (hit != null && hit.getName() != null) {
                category = hit.getName();
            }
        }
        wrapper.eq(Drama::getCategory, category);
    }

    /** 来源平台筛选：空 / "全部" 不限 */
    private void applyPlatform(LambdaQueryWrapper<Drama> wrapper, String rawPlatform) {
        String platform = trim(rawPlatform);
        if (!StringUtils.hasText(platform) || "全部".equals(platform) || "all".equalsIgnoreCase(platform)) {
            return;
        }
        wrapper.eq(Drama::getPlatform, platform);
    }

    /** 排序：hot 按浏览量倒序；newest（默认）按发布时间倒序 */
    private void applySort(LambdaQueryWrapper<Drama> wrapper, String rawSort) {
        if ("hot".equalsIgnoreCase(trim(rawSort))) {
            wrapper.orderByDesc(Drama::getViews, Drama::getCreateTime, Drama::getId);
        } else {
            wrapper.orderByDesc(Drama::getCreateTime, Drama::getId);
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString().trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
