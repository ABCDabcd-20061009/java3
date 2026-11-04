-- ============================================================
-- 首页栏目配置 t_site_config（幂等脚本，可重复执行）
-- 与 config/SiteConfigInit.java 等价；正常情况启动时自动建表植入，无需手工执行。
-- ============================================================

CREATE TABLE IF NOT EXISTS t_site_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  config_type VARCHAR(32) NOT NULL COMMENT '配置类型：nav_menu/shortcut/platform/site_text',
  config_key VARCHAR(64) NOT NULL COMMENT '配置键，同类型内唯一',
  label VARCHAR(128) NULL COMMENT '主文案',
  sub_label VARCHAR(255) NULL COMMENT '副文案（快捷入口 desc）',
  icon_key VARCHAR(32) NULL COMMENT '图标标识',
  target VARCHAR(64) NULL COMMENT '跳转目标',
  sort_no INT DEFAULT 99 COMMENT '排序号，越小越靠前',
  status TINYINT DEFAULT 1 COMMENT '0 停用，1 启用',
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
  UNIQUE KEY uk_type_key (config_type, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页栏目配置';

-- ---- 导航菜单（5 条）----
INSERT INTO t_site_config (config_type, config_key, label, target, sort_no, status, create_time)
SELECT 'nav_menu','square','梦境精选','square',1,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='nav_menu' AND config_key='square');
INSERT INTO t_site_config (config_type, config_key, label, target, sort_no, status, create_time)
SELECT 'nav_menu','favorites','我的收藏','favorites',2,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='nav_menu' AND config_key='favorites');
INSERT INTO t_site_config (config_type, config_key, label, target, sort_no, status, create_time)
SELECT 'nav_menu','profile','我的梦境','profile',3,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='nav_menu' AND config_key='profile');
INSERT INTO t_site_config (config_type, config_key, label, target, sort_no, status, create_time)
SELECT 'nav_menu','drama','广播剧','drama',4,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='nav_menu' AND config_key='drama');
INSERT INTO t_site_config (config_type, config_key, label, target, sort_no, status, create_time)
SELECT 'nav_menu','help','帮助中心','help',5,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='nav_menu' AND config_key='help');

-- ---- 快捷入口（5 条）----
INSERT INTO t_site_config (config_type, config_key, label, sub_label, icon_key, target, sort_no, status, create_time)
SELECT 'shortcut','write_dream','写一场梦','把昨夜的梦记录下来','edit','profile',1,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='shortcut' AND config_key='write_dream');
INSERT INTO t_site_config (config_type, config_key, label, sub_label, icon_key, target, sort_no, status, create_time)
SELECT 'shortcut','go_favorites','我的收藏','回头看看收藏的梦','heart','favorites',2,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='shortcut' AND config_key='go_favorites');
INSERT INTO t_site_config (config_type, config_key, label, sub_label, icon_key, target, sort_no, status, create_time)
SELECT 'shortcut','go_drama','广播剧','戴上耳机听一场梦','play','drama',3,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='shortcut' AND config_key='go_drama');
INSERT INTO t_site_config (config_type, config_key, label, sub_label, icon_key, target, sort_no, status, create_time)
SELECT 'shortcut','go_square','梦境精选','看看大家做了什么梦','star','square',4,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='shortcut' AND config_key='go_square');
INSERT INTO t_site_config (config_type, config_key, label, sub_label, icon_key, target, sort_no, status, create_time)
SELECT 'shortcut','go_profile','个人中心','我的创作与数据','user','profile',5,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='shortcut' AND config_key='go_profile');

-- ---- 广播剧平台（5 条）----
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'platform','platform_1','喜马拉雅',1,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='platform' AND config_key='platform_1');
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'platform','platform_2','猫耳FM',2,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='platform' AND config_key='platform_2');
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'platform','platform_3','晋江文学城',3,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='platform' AND config_key='platform_3');
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'platform','platform_4','蜻蜓FM',4,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='platform' AND config_key='platform_4');
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'platform','platform_5','凯叔讲故事',5,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='platform' AND config_key='platform_5');

-- ---- 站点文案（4 条，键固定，禁止删除）----
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'site_text','brand_name','粉金梦境',1,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='site_text' AND config_key='brand_name');
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'site_text','hero_title','把梦留在云朵上',2,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='site_text' AND config_key='hero_title');
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'site_text','hero_sub','记录、分享、聆听 —— 每一场梦都值得被温柔收藏',3,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='site_text' AND config_key='hero_sub');
INSERT INTO t_site_config (config_type, config_key, label, sort_no, status, create_time)
SELECT 'site_text','footer','粉金梦境 · 愿你夜夜好梦 ✦',4,1,NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_site_config WHERE config_type='site_text' AND config_key='footer');
