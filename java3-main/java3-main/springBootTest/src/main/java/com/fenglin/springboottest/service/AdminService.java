package com.fenglin.springboottest.service;

import com.fenglin.springboottest.dto.AdminCategoryDTO;
import com.fenglin.springboottest.dto.AdminDreamDTO;
import com.fenglin.springboottest.dto.AdminDramaDTO;
import com.fenglin.springboottest.vo.AdminCategoryVO;
import com.fenglin.springboottest.vo.AdminStatsVO;

import java.util.List;
import java.util.Map;

/**
 * 后台管理服务：管理员登录 / 数据概览 / 梦境与广播剧管理 / 分类管理 / 用户管理 / 搜索日志 / 互动数据
 * 约定：列表统一返回 { list, total, page, size, pages, hasMore }
 */
public interface AdminService {

    /** 管理员登录：成功返回 { id, username, token } */
    Map<String, Object> login(String username, String password);

    /** 当前管理员信息：{ id, username } */
    Map<String, Object> info();

    /** 数据概览 8 项统计 */
    AdminStatsVO overview();

    /** 梦境列表（含草稿/下架，keyword/category/status 筛选） */
    Map<String, Object> pageDreams(Integer page, Integer size, String keyword, String category, Integer status);

    /** 保存梦境（有 id 编辑），返回单条 map */
    Map<String, Object> saveDream(AdminDreamDTO dto);

    /** 删除梦境（逻辑删除），返回标题 */
    String deleteDream(Long id);

    /** 广播剧列表 */
    Map<String, Object> pageDramas(Integer page, Integer size, String keyword, String category, String platform, Integer status);

    /** 保存广播剧，返回单条 map */
    Map<String, Object> saveDrama(AdminDramaDTO dto);

    /** 删除广播剧（逻辑删除），返回标题 */
    String deleteDrama(Long id);

    /** 分类列表：type = dream | drama */
    List<AdminCategoryVO> categories(String type);

    /** 保存分类（type 决定写到哪张分类表），返回单条 */
    AdminCategoryVO saveCategory(AdminCategoryDTO dto);

    /** 删除分类（type 决定删哪张表），返回名称 */
    String deleteCategory(Long id, String type);

    /** 用户列表（含已注销，带每人梦境数） */
    Map<String, Object> pageUsers(Integer page, Integer size, String keyword);

    /** 注销用户（逻辑删除），返回用户名 */
    String deleteUser(Long id);

    /** 搜索日志列表 */
    Map<String, Object> pageSearchLogs(Integer page, Integer size, String keyword);

    /** 清空搜索日志 */
    void clearSearchLogs();

    /** 点赞记录列表 */
    Map<String, Object> pageLikes(Integer page, Integer size);

    /** 收藏记录列表 */
    Map<String, Object> pageFavorites(Integer page, Integer size);
}
