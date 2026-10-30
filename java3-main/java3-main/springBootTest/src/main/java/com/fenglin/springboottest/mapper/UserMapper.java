package com.fenglin.springboottest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fenglin.springboottest.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 后台管理：用户列表（带每人的梦境数）。
     * createTime 用 DATE_FORMAT 转成字符串，避免 LocalDateTime 被 Jackson 序列化成数组。
     */
    @Select("<script>" +
            "SELECT u.id AS id, u.username AS username, " +
            "DATE_FORMAT(u.create_time, '%Y-%m-%d %H:%i') AS createTime, " +
            "u.deleted AS deleted, " +
            "(SELECT COUNT(*) FROM t_dream d WHERE d.user_id = u.id AND d.deleted = 0) AS dreamCount " +
            "FROM t_user u " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "</where>" +
            "ORDER BY u.create_time DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Map<String, Object>> selectAdminPage(@Param("keyword") String keyword,
                                              @Param("limit") long limit,
                                              @Param("offset") long offset);

    /** 后台管理：用户列表总数（与上面的筛选条件保持一致） */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_user u " +
            "<where>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND u.username LIKE CONCAT('%', #{keyword}, '%') " +
            "</if>" +
            "</where>" +
            "</script>")
    long selectAdminCount(@Param("keyword") String keyword);
}
