package com.fenglin.springboottest.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.Admin;
import com.fenglin.springboottest.mapper.AdminMapper;
import com.fenglin.springboottest.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 后台管理员账号初始化：应用启动时幂等建表 t_admin，并植入默认管理员。
 * 默认账号 admin / admin123；表内已有任意管理员（表非空）时跳过植入，不覆盖已有账号。
 */
@Component
public class AdminSchemaInit implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSchemaInit.class);

    private final JdbcTemplate jdbc;
    private final AdminMapper adminMapper;

    public AdminSchemaInit(JdbcTemplate jdbc, AdminMapper adminMapper) {
        this.jdbc = jdbc;
        this.adminMapper = adminMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        createTableIfAbsent();
        seed();
    }

    private void createTableIfAbsent() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS t_admin (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) NOT NULL," +
                "password VARCHAR(128) NOT NULL," +
                "create_time DATETIME NULL," +
                "update_time DATETIME NULL," +
                "deleted TINYINT DEFAULT 0," +
                "UNIQUE KEY uk_admin_username (username)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void seed() {
        Long count = adminMapper.selectCount(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, "admin"));
        if (count != null && count > 0) {
            return;   // 默认管理员已存在，不覆盖
        }
        Admin admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword(PasswordUtil.encode("admin123"));
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        adminMapper.insert(admin);
        log.info("已植入默认管理员 admin / admin123（t_admin）");
    }
}
