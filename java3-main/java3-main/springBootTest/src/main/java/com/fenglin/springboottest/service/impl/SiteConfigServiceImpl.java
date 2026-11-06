package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.common.BizException;
import com.fenglin.springboottest.dto.SiteConfigDTO;
import com.fenglin.springboottest.entity.SiteConfig;
import com.fenglin.springboottest.mapper.SiteConfigMapper;
import com.fenglin.springboottest.service.SiteConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 首页栏目配置实现：
 * - 前台输出手工用 LinkedHashMap 组装，不直接抛实体，规避 LocalDateTime 被 Jackson 序列化成数组
 * - 站点文案（site_text）是前端强依赖的固定键，禁止删除，只能改内容或停用
 */
@Service
public class SiteConfigServiceImpl implements SiteConfigService {

    /** 合法的配置类型 */
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
            SiteConfig.TYPE_NAV_MENU, SiteConfig.TYPE_SHORTCUT,
            SiteConfig.TYPE_PLATFORM, SiteConfig.TYPE_SITE_TEXT));

    /** 站点文案的固定键：删了首页会留白 */
    private static final Set<String> SITE_TEXT_KEYS = new HashSet<>(Arrays.asList(
            "brand_name", "hero_sub", "footer"));

    private final SiteConfigMapper mapper;

    public SiteConfigServiceImpl(SiteConfigMapper mapper) {
        this.mapper = mapper;
    }

    // ==================== 前台 ====================

    @Override
    public Map<String, Object> frontConfig() {
        List<SiteConfig> all = mapper.selectList(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getStatus, 1)
                .orderByAsc(SiteConfig::getSortNo, SiteConfig::getId));

        List<Map<String, Object>> navMenu = new ArrayList<>();
        List<Map<String, Object>> shortcut = new ArrayList<>();
        List<String> platform = new ArrayList<>();
        Map<String, Object> text = new LinkedHashMap<>();

        for (SiteConfig c : all) {
            String type = c.getConfigType();
            if (SiteConfig.TYPE_NAV_MENU.equals(type)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("key", c.getConfigKey());
                item.put("label", c.getLabel());
                item.put("target", c.getTarget());
                navMenu.add(item);
            } else if (SiteConfig.TYPE_SHORTCUT.equals(type)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("key", c.getConfigKey());
                item.put("title", c.getLabel());
                item.put("desc", c.getSubLabel());
                item.put("iconKey", c.getIconKey());
                item.put("target", c.getTarget());
                shortcut.add(item);
            } else if (SiteConfig.TYPE_PLATFORM.equals(type)) {
                platform.add(c.getLabel());
            } else if (SiteConfig.TYPE_SITE_TEXT.equals(type)) {
                text.put(c.getConfigKey(), c.getLabel());
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("navMenu", navMenu);
        data.put("shortcut", shortcut);
        data.put("platform", platform);
        data.put("text", text);
        return data;
    }

    // ==================== 后台 ====================

    @Override
    public List<Map<String, Object>> list(String type) {
        String t = trim(type);
        if (!TYPES.contains(t)) {
            throw new BizException("非法的配置类型：" + t);
        }
        List<SiteConfig> rows = mapper.selectList(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigType, t)
                .orderByAsc(SiteConfig::getSortNo, SiteConfig::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (SiteConfig c : rows) {
            list.add(toMap(c));
        }
        return list;
    }

    @Override
    public Map<String, Object> save(SiteConfigDTO dto) {
        if (dto == null) {
            throw new BizException("缺少配置内容");
        }
        String type = trim(dto.getConfigType());
        if (!TYPES.contains(type)) {
            throw new BizException("非法的配置类型：" + type);
        }
        String label = trim(dto.getLabel());
        if (label == null || label.isEmpty()) {
            throw new BizException("请填写文案内容");
        }
        // 站点文案的 configKey 是前端依赖的固定键
        String key = trim(dto.getConfigKey());
        if (SiteConfig.TYPE_SITE_TEXT.equals(type)) {
            if (key == null || !SITE_TEXT_KEYS.contains(key)) {
                throw new BizException("站点文案的键必须固定为：" + String.join(" / ", SITE_TEXT_KEYS));
            }
        } else if (key == null || key.isEmpty()) {
            key = type + "_" + System.currentTimeMillis();
        }

        SiteConfig config;
        if (dto.getId() != null) {
            config = mapper.selectById(dto.getId());
            if (config == null) {
                throw new BizException("配置项不存在或已删除");
            }
            if (!type.equals(config.getConfigType())) {
                throw new BizException("不允许修改配置类型");
            }
        } else {
            config = new SiteConfig();
            config.setConfigType(type);
            config.setCreateTime(LocalDateTime.now());
        }

        // configKey 同类型内唯一（编辑时排除自身）
        SiteConfig dup = mapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigType, type)
                .eq(SiteConfig::getConfigKey, key)
                .ne(config.getId() != null, SiteConfig::getId, config.getId())
                .last("limit 1"));
        if (dup != null) {
            throw new BizException("配置键「" + key + "」已存在");
        }

        config.setConfigKey(key);
        config.setLabel(label);
        config.setSubLabel(trim(dto.getSubLabel()));
        config.setIconKey(trim(dto.getIconKey()));
        config.setTarget(trim(dto.getTarget()));
        config.setSortNo(dto.getSortNo() == null ? 99 : dto.getSortNo());
        config.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        config.setUpdateTime(LocalDateTime.now());

        if (config.getId() == null) {
            mapper.insert(config);
        } else {
            mapper.updateById(config);
        }
        return toMap(mapper.selectById(config.getId()));
    }

    @Override
    public String delete(Long id) {
        if (id == null) {
            throw new BizException("缺少配置ID");
        }
        SiteConfig config = mapper.selectById(id);
        if (config == null) {
            throw new BizException("配置项不存在或已删除");
        }
        if (SiteConfig.TYPE_SITE_TEXT.equals(config.getConfigType())) {
            throw new BizException("站点文案不允许删除，只能修改内容或停用");
        }
        String label = config.getLabel();
        mapper.deleteById(id);
        return label;
    }

    // ==================== 内部工具 ====================

    /** 手工组装返回体，不直接抛实体（规避 LocalDateTime 序列化成数组） */
    private Map<String, Object> toMap(SiteConfig c) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", c.getId());
        item.put("configType", c.getConfigType());
        item.put("configKey", c.getConfigKey());
        item.put("label", c.getLabel());
        item.put("subLabel", c.getSubLabel());
        item.put("iconKey", c.getIconKey());
        item.put("target", c.getTarget());
        item.put("sortNo", c.getSortNo());
        item.put("status", c.getStatus());
        return item;
    }

    private static String trim(String s) {
        return (s == null) ? null : s.trim();
    }
}
