package com.fenglin.springboottest.config;

import com.fenglin.springboottest.entity.SiteConfig;
import com.fenglin.springboottest.mapper.SiteConfigMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 首页栏目配置初始化：应用启动时幂等建表 t_site_config，并植入种子数据。
 * 只补齐缺失项（按 config_type + config_key 判断），不会覆盖人工改动过的数据。
 *
 * 种子共 19 条：5 导航 + 5 快捷入口 + 5 广播剧平台 + 4 站点文案。
 */
@Component
public class SiteConfigInit implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SiteConfigInit.class);

    private final JdbcTemplate jdbc;
    private final SiteConfigMapper mapper;

    public SiteConfigInit(JdbcTemplate jdbc, SiteConfigMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        createTableIfAbsent();
        seed();
    }

    private void createTableIfAbsent() {
        jdbc.execute("CREATE TABLE IF NOT EXISTS t_site_config (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "config_type VARCHAR(32) NOT NULL," +
                "config_key VARCHAR(64) NOT NULL," +
                "label VARCHAR(128) NULL," +
                "sub_label VARCHAR(255) NULL," +
                "icon_key VARCHAR(32) NULL," +
                "target VARCHAR(64) NULL," +
                "sort_no INT DEFAULT 99," +
                "status TINYINT DEFAULT 1," +
                "create_time DATETIME NULL," +
                "update_time DATETIME NULL," +
                "deleted TINYINT DEFAULT 0," +
                "UNIQUE KEY uk_type_key (config_type, config_key)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
    }

    private void seed() {
        for (SiteConfig c : seeds()) {
            Long count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM t_site_config WHERE config_type = ? AND config_key = ?",
                    Long.class, c.getConfigType(), c.getConfigKey());
            if (count != null && count > 0) {
                continue;   // 已存在（含被人工停用/改名的），不覆盖
            }
            c.setCreateTime(LocalDateTime.now());
            c.setUpdateTime(LocalDateTime.now());
            mapper.insert(c);
        }
        log.info("首页栏目配置初始化完成（t_site_config）");
    }

    private List<SiteConfig> seeds() {
        return Arrays.asList(
                // ---- 导航菜单 nav_menu（5 条）----
                nav("square", "动漫人物", "square", 1),
                nav("favorites", "我的收藏", "favorites", 2),
                nav("profile", "我的梦境", "profile", 3),
                nav("drama", "广播剧", "drama", 4),
                nav("help", "帮助中心", "help", 5),
                // ---- 快捷入口 shortcut（5 条）----
                shortcut("write_dream", "写一场梦", "把昨夜的梦记录下来", "edit", "profile", 1),
                shortcut("go_favorites", "我的收藏", "回头看看收藏的梦", "heart", "favorites", 2),
                shortcut("go_drama", "广播剧", "戴上耳机听一场梦", "play", "drama", 3),
                shortcut("go_square", "动漫人物", "看看喜欢的动漫角色", "star", "square", 4),
                shortcut("go_profile", "个人中心", "我的创作与数据", "user", "profile", 5),
                // ---- 广播剧平台 platform（5 条）----
                platform("喜马拉雅", 1),
                platform("猫耳FM", 2),
                platform("晋江文学城", 3),
                platform("蜻蜓FM", 4),
                platform("凯叔讲故事", 5),
                // ---- 站点文案 site_text（3 条，键固定）----
                text("brand_name", "粉金梦境", 1),
                text("hero_sub", "记录、分享、聆听 —— 每一场梦都值得被温柔收藏", 2),
                text("footer", "粉金梦境 · 愿你夜夜好梦 ✦", 3));
    }

    private SiteConfig nav(String key, String label, String target, int sortNo) {
        SiteConfig c = base(SiteConfig.TYPE_NAV_MENU, key, label, sortNo);
        c.setTarget(target);
        return c;
    }

    private SiteConfig shortcut(String key, String title, String desc, String iconKey, String target, int sortNo) {
        SiteConfig c = base(SiteConfig.TYPE_SHORTCUT, key, title, sortNo);
        c.setSubLabel(desc);
        c.setIconKey(iconKey);
        c.setTarget(target);
        return c;
    }

    private SiteConfig platform(String name, int sortNo) {
        return base(SiteConfig.TYPE_PLATFORM, "platform_" + sortNo, name, sortNo);
    }

    private SiteConfig text(String key, String value, int sortNo) {
        return base(SiteConfig.TYPE_SITE_TEXT, key, value, sortNo);
    }

    private SiteConfig base(String type, String key, String label, int sortNo) {
        SiteConfig c = new SiteConfig();
        c.setConfigType(type);
        c.setConfigKey(key);
        c.setLabel(label);
        c.setSortNo(sortNo);
        c.setStatus(1);
        return c;
    }
}
