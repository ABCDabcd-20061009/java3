package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.SiteConfigDTO;

import java.util.List;
import java.util.Map;

/**
 * 首页栏目配置服务：
 * - 前台 /api/home/config 输出 navMenu / shortcut / platform / text 四块
 * - 后台 /api/admin/home-config/** 维护配置项
 */
public interface SiteConfigService {

    /**
     * 前台首页栏目配置（匿名可访问）：只返回启用的配置项。
     * data = {
     *   navMenu:  [ { key, label, target } ],
     *   shortcut: [ { key, title, desc, iconKey, target } ],
     *   platform: [ "喜马拉雅", ... ],
     *   text:     { brand_name, hero_title, hero_sub, footer }
     * }
     */
    Map<String, Object> frontConfig();

    /** 后台：按类型列出配置项（含停用项，按 sortNo 排序） */
    List<Map<String, Object>> list(String type);

    /** 后台：保存（有 id 编辑，无 id 新增），返回保存后的单条 */
    Map<String, Object> save(SiteConfigDTO dto);

    /** 后台：删除（逻辑删除）；站点文案禁止删除，返回被删配置的 label */
    String delete(Long id);
}
