package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.common.Result;
import com.fenglin.springboottest.dto.AdminCategoryDTO;
import com.fenglin.springboottest.dto.AdminDreamDTO;
import com.fenglin.springboottest.dto.AdminDramaDTO;
import com.fenglin.springboottest.dto.AdminLoginDTO;
import com.fenglin.springboottest.service.AdminService;
import com.fenglin.springboottest.vo.AdminCategoryVO;
import com.fenglin.springboottest.vo.AdminStatsVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理接口（/api/admin/**，共 18 个）。
 * 鉴权由 JwtInterceptor 统一处理：除 /api/admin/login 外，都要求 token 里 role=admin。
 * 约定：POST JSON，统一 { code, message, data } 响应。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** 管理员登录（唯一匿名入口）：{ username, password } → { id, username, token } */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody AdminLoginDTO dto) {
        return Result.ok("登录成功", adminService.login(
                dto == null ? null : dto.getUsername(),
                dto == null ? null : dto.getPassword()));
    }

    /** 当前管理员信息（需登录） */
    @PostMapping("/info")
    public Result<Map<String, Object>> info() {
        return Result.ok("ok", adminService.info());
    }

    /** 数据概览 8 项统计 */
    @PostMapping("/overview")
    public Result<AdminStatsVO> overview() {
        return Result.ok("ok", adminService.overview());
    }

    /* ---- 梦境管理 ---- */

    /** 梦境列表：{ page, size, keyword?, category?, status? } */
    @PostMapping("/dreams")
    public Result<Map<String, Object>> dreams(@RequestBody(required = false) Map<String, Object> body) {
        return Result.ok("ok", adminService.pageDreams(
                intOf(body, "page"), intOf(body, "size"), strOf(body, "keyword"),
                strOf(body, "category"), intOf(body, "status")));
    }

    /** 保存梦境（有 id 编辑）：AdminDreamDTO */
    @PostMapping("/dream/save")
    public Result<Map<String, Object>> saveDream(@RequestBody AdminDreamDTO dto) {
        return Result.ok("保存成功", adminService.saveDream(dto));
    }

    /** 删除梦境（逻辑删除）：{ id } */
    @PostMapping("/dream/delete")
    public Result<Map<String, Object>> deleteDream(@RequestBody(required = false) Map<String, Object> body) {
        Long id = longOf(body, "id");
        String title = adminService.deleteDream(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", title);
        return Result.ok("已删除「" + title + "」", data);
    }

    /* ---- 广播剧管理 ---- */

    /** 广播剧列表：{ page, size, keyword?, category?, platform?, status? } */
    @PostMapping("/dramas")
    public Result<Map<String, Object>> dramas(@RequestBody(required = false) Map<String, Object> body) {
        return Result.ok("ok", adminService.pageDramas(
                intOf(body, "page"), intOf(body, "size"), strOf(body, "keyword"),
                strOf(body, "category"), strOf(body, "platform"), intOf(body, "status")));
    }

    /** 保存广播剧（有 id 编辑）：AdminDramaDTO */
    @PostMapping("/drama/save")
    public Result<Map<String, Object>> saveDrama(@RequestBody AdminDramaDTO dto) {
        return Result.ok("保存成功", adminService.saveDrama(dto));
    }

    /** 删除广播剧（逻辑删除）：{ id } */
    @PostMapping("/drama/delete")
    public Result<Map<String, Object>> deleteDrama(@RequestBody(required = false) Map<String, Object> body) {
        Long id = longOf(body, "id");
        String title = adminService.deleteDrama(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("title", title);
        return Result.ok("已删除「" + title + "」", data);
    }

    /* ---- 分类管理 ---- */

    /** 分类列表：{ type: dream | drama } */
    @PostMapping("/categories")
    public Result<List<AdminCategoryVO>> categories(@RequestBody(required = false) Map<String, Object> body) {
        return Result.ok("ok", adminService.categories(strOf(body, "type")));
    }

    /** 保存分类（有 id 编辑）：AdminCategoryDTO（type 决定写到哪张分类表） */
    @PostMapping("/category/save")
    public Result<AdminCategoryVO> saveCategory(@RequestBody AdminCategoryDTO dto) {
        return Result.ok("保存成功", adminService.saveCategory(dto));
    }

    /** 删除分类：{ id, type }（分类下还有内容时拒绝） */
    @PostMapping("/category/delete")
    public Result<Map<String, Object>> deleteCategory(@RequestBody(required = false) Map<String, Object> body) {
        Long id = longOf(body, "id");
        String name = adminService.deleteCategory(id, strOf(body, "type"));
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", name);
        return Result.ok("已删除「" + name + "」", data);
    }

    /* ---- 用户管理 ---- */

    /** 用户列表（含已注销）：{ page, size, keyword? } */
    @PostMapping("/users")
    public Result<Map<String, Object>> users(@RequestBody(required = false) Map<String, Object> body) {
        return Result.ok("ok", adminService.pageUsers(
                intOf(body, "page"), intOf(body, "size"), strOf(body, "keyword")));
    }

    /** 注销用户（逻辑删除）：{ id } */
    @PostMapping("/user/delete")
    public Result<Map<String, Object>> deleteUser(@RequestBody(required = false) Map<String, Object> body) {
        Long id = longOf(body, "id");
        String username = adminService.deleteUser(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("username", username);
        return Result.ok("已注销「" + username + "」", data);
    }

    /* ---- 搜索日志 ---- */

    /** 搜索日志：{ page, size, keyword? } */
    @PostMapping("/search-logs")
    public Result<Map<String, Object>> searchLogs(@RequestBody(required = false) Map<String, Object> body) {
        return Result.ok("ok", adminService.pageSearchLogs(
                intOf(body, "page"), intOf(body, "size"), strOf(body, "keyword")));
    }

    /** 清空搜索日志 */
    @PostMapping("/search-logs/clear")
    public Result<Void> clearSearchLogs() {
        adminService.clearSearchLogs();
        return Result.ok("搜索日志已清空");
    }

    /* ---- 互动数据 ---- */

    /** 点赞记录：{ page, size } */
    @PostMapping("/likes")
    public Result<Map<String, Object>> likes(@RequestBody(required = false) Map<String, Object> body) {
        return Result.ok("ok", adminService.pageLikes(intOf(body, "page"), intOf(body, "size")));
    }

    /** 收藏记录：{ page, size } */
    @PostMapping("/favorites")
    public Result<Map<String, Object>> favorites(@RequestBody(required = false) Map<String, Object> body) {
        return Result.ok("ok", adminService.pageFavorites(intOf(body, "page"), intOf(body, "size")));
    }

    /* ---- 参数解析工具 ---- */

    private static String strOf(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return null;
        }
        Object v = body.get(key);
        String s = String.valueOf(v).trim();
        return s.isEmpty() ? null : s;
    }

    private static Integer intOf(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(body.get(key)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Long longOf(Map<String, Object> body, String key) {
        if (body == null || body.get(key) == null) {
            throw new com.fenglin.springboottest.common.BizException("缺少参数 " + key);
        }
        try {
            return Long.valueOf(String.valueOf(body.get(key)));
        } catch (NumberFormatException e) {
            throw new com.fenglin.springboottest.common.BizException("参数 " + key + " 不合法");
        }
    }
}
