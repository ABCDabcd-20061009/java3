package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.SiteConfigDTO;
import com.fenglin.springboottest.service.SiteConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 首页栏目配置的后台维护接口（/api/admin/home-config/**）。
 * 鉴权由 JwtInterceptor 统一处理：/api/admin/** 除登录外都要求 role=admin 的 token。
 * 约定：POST JSON，统一 { code, message, data } 响应。
 */
@RestController
@RequestMapping("/api/admin/home-config")
public class SiteConfigController {

    private final SiteConfigService siteConfigService;

    public SiteConfigController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    /**
     * 配置列表：{ type: nav_menu | shortcut | platform | site_text }
     * 返回 [{id, configType, configKey, label, subLabel, iconKey, target, sortNo, status}]
     */
    @PostMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestBody(required = false) Map<String, Object> body) {
        String type = body == null ? null : (String) body.get("type");
        return Result.ok("ok", siteConfigService.list(type));
    }

    /**
     * 保存配置（有 id 编辑，无 id 新增）：SiteConfigDTO
     */
    @PostMapping("/save")
    public Result<Map<String, Object>> save(@RequestBody SiteConfigDTO dto) {
        return Result.ok("保存成功", siteConfigService.save(dto));
    }

    /**
     * 删除配置：{ id }（逻辑删除；站点文案禁止删除）
     */
    @PostMapping("/delete")
    public Result<Map<String, Object>> delete(@RequestBody(required = false) Map<String, Object> body) {
        Long id = body == null || body.get("id") == null ? null : Long.valueOf(String.valueOf(body.get("id")));
        String label = siteConfigService.delete(id);
        return Result.ok("已删除「" + label + "」", Map.of("id", id, "label", label == null ? "" : label));
    }
}
