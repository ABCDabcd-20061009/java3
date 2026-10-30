package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DreamFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DreamFavoriteMapper extends BaseMapper<DreamFavorite> {

    /** 后台管理：收藏记录列表，联表带出梦境标题与用户名；createTime 转字符串 */
    @Select("SELECT f.id AS id, f.dream_id AS dreamId, d.title AS dreamTitle, " +
            "f.user_id AS userId, u.username AS username, " +
            "DATE_FORMAT(f.create_time, '%Y-%m-%d %H:%i') AS createTime " +
            "FROM t_dream_favorite f " +
            "LEFT JOIN t_dream d ON d.id = f.dream_id " +
            "LEFT JOIN t_user u ON u.id = f.user_id " +
            "ORDER BY f.create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> selectAdminPage(@Param("limit") long limit, @Param("offset") long offset);

    /** 后台管理：收藏记录总数 */
    @Select("SELECT COUNT(*) FROM t_dream_favorite")
    long selectAdminCount();
}
