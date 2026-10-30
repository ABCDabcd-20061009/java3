package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.DreamLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DreamLikeMapper extends BaseMapper<DreamLike> {

    /** 后台管理：点赞记录列表，联表带出梦境标题与用户名；createTime 转字符串 */
    @Select("SELECT l.id AS id, l.dream_id AS dreamId, d.title AS dreamTitle, " +
            "l.user_id AS userId, u.username AS username, " +
            "DATE_FORMAT(l.create_time, '%Y-%m-%d %H:%i') AS createTime " +
            "FROM t_dream_like l " +
            "LEFT JOIN t_dream d ON d.id = l.dream_id " +
            "LEFT JOIN t_user u ON u.id = l.user_id " +
            "ORDER BY l.create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Map<String, Object>> selectAdminPage(@Param("limit") long limit, @Param("offset") long offset);

    /** 后台管理：点赞记录总数 */
    @Select("SELECT COUNT(*) FROM t_dream_like")
    long selectAdminCount();
}
