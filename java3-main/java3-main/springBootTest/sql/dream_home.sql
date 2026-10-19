-- =========================================================
-- 粉金梦境 首页功能建表脚本（首页展示 / 搜索 / 点赞 / 收藏 / 热门词）
-- 数据库：dream_login    字符集：utf8mb4
-- 本脚本幂等：建表用 IF NOT EXISTS，种子数据用 INSERT IGNORE，可重复执行
-- 执行方式（任选其一）：
--   1) 命令行：mysql -uroot -p123456 --default-character-set=utf8mb4 dream_login < dream_home.sql
--   2) 在 IDEA / Navicat / DataGrip 中直接执行本文件
-- 依赖：先执行 dream_login.sql（建库 + t_user），再执行本脚本
-- =========================================================

USE dream_login;

-- ---------------------------------------------------------
-- 1. t_dream 梦境内容表 —— 首页展示与搜索的数据源
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_dream (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL DEFAULT 0  COMMENT '作者用户ID',
    author_name VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '作者昵称（发布时冗余存储）',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    summary     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '摘要',
    content     TEXT         NOT NULL COMMENT '正文',
    cover_url   VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图URL（为空时前端用渐变占位）',
    category    VARCHAR(30)  NOT NULL DEFAULT '随笔' COMMENT '分类：美梦/噩梦/灵感/随笔',
    tags        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '标签，逗号分隔',
    status      TINYINT      NOT NULL DEFAULT 1  COMMENT '0 草稿，1 发布',
    views       INT          NOT NULL DEFAULT 0  COMMENT '浏览量',
    likes       INT          NOT NULL DEFAULT 0  COMMENT '点赞数',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除：0 正常，1 已删除',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_status_ctime (status, create_time),
    KEY idx_status_views (status, views),
    KEY idx_author (author_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='梦境内容表（首页展示/搜索）';

-- ---------------------------------------------------------
-- 2. t_dream_category 分类字典表 —— 首页分类导航与数量统计
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_dream_category (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        VARCHAR(20) NOT NULL COMMENT '分类编码：beauty/nightmare/inspiration/essay',
    name        VARCHAR(20) NOT NULL COMMENT '分类名称：美梦/噩梦/灵感/随笔',
    icon        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '图标标识（前端映射 SVG）',
    sort_no     INT         NOT NULL DEFAULT 0  COMMENT '排序号，越小越靠前',
    status      TINYINT     NOT NULL DEFAULT 1  COMMENT '0 停用，1 启用',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='梦境分类字典表（首页分类导航）';

-- ---------------------------------------------------------
-- 3. t_dream_like 点赞记录表 —— 同一用户对同一梦境只能点一次
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_dream_like (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    dream_id    BIGINT   NOT NULL COMMENT '梦境ID',
    user_id     BIGINT   NOT NULL COMMENT '点赞用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dream_user (dream_id, user_id),
    KEY idx_user (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='梦境点赞记录表';

-- ---------------------------------------------------------
-- 4. t_dream_search_log 搜索关键词日志表 —— 热门搜索词的统计来源
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_dream_search_log (
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    keyword      VARCHAR(50) NOT NULL COMMENT '搜索关键词',
    user_id      BIGINT      NOT NULL DEFAULT 0 COMMENT '搜索用户ID（0 表示未登录）',
    result_count INT         NOT NULL DEFAULT 0 COMMENT '该次搜索命中结果数',
    create_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (id),
    KEY idx_keyword_time (keyword, create_time),
    KEY idx_ctime (create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='搜索关键词日志表（热门搜索词统计）';

-- ---------------------------------------------------------
-- 5. t_dream_favorite 收藏记录表 —— 同一用户对同一梦境只能收藏一次
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_dream_favorite (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    dream_id    BIGINT   NOT NULL COMMENT '梦境ID',
    user_id     BIGINT   NOT NULL COMMENT '收藏用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dream_user (dream_id, user_id),
    KEY idx_user_ctime (user_id, create_time)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='梦境收藏记录表';

-- =========================================================
-- 种子数据（仅首次执行时插入，重复执行会被 IGNORE 跳过）
-- =========================================================

-- 分类字典
INSERT IGNORE INTO t_dream_category (id, code, name, icon, sort_no, status) VALUES
    (1, 'beauty',      '美梦', 'beauty',      1, 1),
    (2, 'nightmare',   '噩梦', 'nightmare',   2, 1),
    (3, 'inspiration', '灵感', 'inspiration', 3, 1),
    (4, 'essay',       '随笔', 'essay',       4, 1);

-- 梦境内容（首页默认展示 + 搜索命中）
INSERT IGNORE INTO t_dream
    (id, user_id, author_name, title, summary, content, cover_url, category, tags, status, views, likes, create_time)
VALUES
    (1, 1, '梦梦', '在云端城堡的午后', '一场关于漂浮、光与软绵绵午后的梦。',
     '我梦见自己住在一座浮在云海上的城堡里。午后的阳光被云朵过滤成蜜糖色，落在毛茸茸的地毯上。\n我赤脚走过回廊，每一扇窗外面都是缓缓流淌的云河。风很轻，带着一点点棉花糖的甜味。\n醒来时，枕边竟还残留着那种暖洋洋的安心。',
     '', '美梦', '城堡,云,午后', 1, 328, 56, '2026-09-28 21:14:00'),

    (2, 2, '星星', '写给未来的一封信', '把还没发生的事，先温柔地写下来。',
     '如果未来的你正读到这封信，请记得：你曾经那么认真地相信过好事会发生。\n无论此刻你在哪里，都别急着否定那个爱做梦的自己。\n梦不是逃避，是给生活留的一扇窗。',
     '', '灵感', '信,未来,温柔', 1, 512, 89, '2026-09-30 08:42:00'),

    (3, 1, '梦梦', '迷雾列车', '一趟永远到不了站的夜车。',
     '梦里我上了一列没有终点的列车，窗外的雾白得发亮。\n车厢里很安静，只有车轮碾过轨道的节奏。我想下车，门却怎么也打不开。\n后来我才明白，有些迷路，本身就是旅程的一部分。',
     '', '噩梦', '列车,迷雾,夜', 1, 176, 21, '2026-10-01 02:30:00'),

    (4, 3, '夜风', '关于星星的碎碎念', '它们那么远，却愿意整夜陪着你。',
     '小时候以为星星是别处的人点亮的灯。\n长大后才发现，它们更像是宇宙写给我们的便签：你并不孤单。\n今晚也抬头看看吧，总有一颗在为你亮着。',
     '', '随笔', '星星,夜,温柔', 1, 240, 47, '2026-10-02 23:05:00'),

    (5, 2, '星星', '会飞的鲸鱼', '当庞然大物也学会了轻盈。',
     '海平面裂开一道口子，一头鲸鱼缓缓升空，像一片被风吹起的影子。\n它从我的窗前游过，尾巴轻轻一摆，洒下满屋的星屑。\n那一刻我忽然觉得，沉重的东西也可以很自由。',
     '', '美梦', '鲸鱼,飞行,星屑', 1, 689, 132, '2026-10-03 20:18:00'),

    (6, 3, '夜风', '把梦画下来', '让睡醒后还在发光的画面留下来。',
     '醒来第一件事，是把梦里那片紫色的海画在便签上。\n颜色会褪色，但手感不会。\n把梦画下来，就像给了它第二次生命。',
     '', '灵感', '画画,梦,紫色', 1, 154, 30, '2026-10-05 09:50:00'),

    (7, 1, '梦梦', '深夜电台', '凌晨三点，总有人在电波那头说话。',
     '调到一个没有台号的频率，里面有人在念陌生人的心事。\n沙沙的杂音里，是无数个同样睡不着的灵魂。\n原来失眠也可以很浪漫。',
     '', '随笔', '电台,深夜,心事', 1, 203, 38, '2026-10-06 03:12:00'),

    (8, 2, '星星', '无人的游乐园', '旋转木马还在转，却没有观众。',
     '我走进一座荒废的游乐园，霓虹灯一闪一闪，音乐盒自己唱着歌。\n旋转木马空着，却依旧优雅地转圈。\n孤单不一定是冷的，也可以很安静、很美。',
     '', '噩梦', '游乐园,旋转木马,孤独', 1, 98, 12, '2026-10-07 01:47:00'),

    (9, 3, '夜风', '月光编织的毛衣', '把温柔穿在身上过冬。',
     '梦见一位老奶奶用月光做线，给我织了一件会发光的毛衣。\n穿上它，整个人都暖暖的，连影子都是软的。\n愿每个人，都能拥有这样一件看不见的毛衣。',
     '', '美梦', '月光,毛衣,温暖', 1, 421, 77, '2026-10-08 22:33:00'),

    (10, 1, '梦梦', '梦境收集者手记', '我们都是偷偷收藏梦的人。',
     '如果你也常做奇怪的梦，欢迎写下来，贴在这面墙上。\n这里没有好坏，只有真实。\n每一个梦，都是一个平行宇宙寄来的明信片。',
     '', '灵感', '收集,手记,平行宇宙', 1, 567, 101, '2026-10-09 19:20:00'),

    (11, 2, '星星', '糖果雨停之后', '整条街都在舔手指。',
     '城市下了三天的糖果雨，屋檐上黏着彩虹色的糖浆。\n孩子们撑开棒棒糖伞，把落下来的软糖装进口袋。\n雨停之后，空气里全是甜的，连路灯都在发光。',
     '', '美梦', '糖果,雨,童话', 1, 305, 64, '2026-10-11 15:26:00'),

    (12, 3, '夜风', '月亮图书馆', '每一本书里都住着一个月亮。',
     '推开那扇很旧的木门，书架一直长到看不见的顶上。\n随手抽出一本，里面的月亮就跟着我走。\n管理员说：别急着还，今晚你梦见哪里，就是还给我的方式。',
     '', '随笔', '月亮,图书馆,安静', 1, 268, 52, '2026-10-13 21:05:00');

-- 点赞记录（种子：仅作互动演示，t_dream.likes 为历史累计数，二者相加呈现）
INSERT IGNORE INTO t_dream_like (id, dream_id, user_id, create_time) VALUES
    (1, 1, 2, '2026-09-29 10:02:00'),
    (2, 1, 3, '2026-09-29 21:31:00'),
    (3, 5, 1, '2026-10-04 09:12:00'),
    (4, 5, 3, '2026-10-04 19:45:00'),
    (5, 10, 2, '2026-10-10 08:20:00');

-- 搜索关键词日志（种子：让首页「热门搜索」开箱即有数据）
INSERT IGNORE INTO t_dream_search_log (id, keyword, user_id, result_count, create_time) VALUES
    (1,  '云朵', 1, 1, '2026-10-10 09:00:00'),
    (2,  '云朵', 2, 1, '2026-10-11 10:20:00'),
    (3,  '云朵', 3, 1, '2026-10-12 21:05:00'),
    (4,  '星星', 1, 3, '2026-10-11 22:14:00'),
    (5,  '星星', 2, 3, '2026-10-12 08:40:00'),
    (6,  '月亮', 3, 2, '2026-10-12 23:30:00'),
    (7,  '月亮', 1, 2, '2026-10-13 20:11:00'),
    (8,  '糖果', 2, 2, '2026-10-13 09:26:00'),
    (9,  '鲸鱼', 3, 1, '2026-10-14 21:48:00'),
    (10, '梦',   1, 6, '2026-10-14 22:02:00'),
    (11, '梦',   2, 6, '2026-10-15 08:15:00'),
    (12, '温柔', 3, 2, '2026-10-15 21:33:00'),
    (13, '云朵', 1, 1, '2026-10-15 22:40:00'),
    (14, '星星', 3, 3, '2026-10-16 09:12:00');

-- 收藏记录（种子：让「我的收藏」开箱即有内容 —— 用户1 收藏了 2、5、10）
INSERT IGNORE INTO t_dream_favorite (id, dream_id, user_id, create_time) VALUES
    (1, 2,  1, '2026-10-10 10:05:00'),
    (2, 5,  1, '2026-10-11 21:20:00'),
    (3, 10, 1, '2026-10-12 08:36:00'),
    (4, 9,  2, '2026-10-13 22:14:00'),
    (5, 1,  3, '2026-10-14 19:02:00');
