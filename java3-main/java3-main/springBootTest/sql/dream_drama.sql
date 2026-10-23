-- =========================================================
-- 粉金梦境 广播剧功能建表脚本（广播剧浏览 / 分类 / 详情）
-- 数据库：dream_login    字符集：utf8mb4
-- 本脚本幂等：建表用 IF NOT EXISTS，种子数据用 INSERT IGNORE，可重复执行
-- 执行方式：
--   mysql -uroot -p123456 --default-character-set=utf8mb4 dream_login < dream_drama.sql
-- 依赖：先执行 dream_login.sql（建库+用户）与 dream_home.sql（梦境表）
-- 说明：video_url 暂用公开示例视频（仅作演示占位），上线前请替换为真实广播剧媒体。
-- =========================================================

USE dream_login;

-- ---------------------------------------------------------
-- 1. t_drama_category 广播剧分类字典表
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_drama_category (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    code        VARCHAR(20) NOT NULL COMMENT '分类编码：suspense/romance/sci-fi/healing/ancient/children',
    name        VARCHAR(20) NOT NULL COMMENT '分类名称：悬疑/言情/科幻/治愈/古风/儿童',
    icon        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '图标标识（前端映射 SVG）',
    sort_no     INT         NOT NULL DEFAULT 0  COMMENT '排序号，越小越靠前',
    status      TINYINT     NOT NULL DEFAULT 1  COMMENT '0 停用，1 启用',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='广播剧分类字典表';

-- ---------------------------------------------------------
-- 2. t_drama 广播剧内容表 —— 首页「广播剧」的数据源（有画面也有声音）
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_drama (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    summary     VARCHAR(255) NOT NULL DEFAULT '' COMMENT '一句话简介',
    cover_url   VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图URL（为空时前端用分类渐变占位）',
    category    VARCHAR(30)  NOT NULL DEFAULT '治愈' COMMENT '分类：悬疑/言情/科幻/治愈/古风/儿童',
    tags        VARCHAR(255) NOT NULL DEFAULT '' COMMENT '标签，逗号分隔',
    platform    VARCHAR(30)  NOT NULL DEFAULT '' COMMENT '来源平台：喜马拉雅/猫耳FM/晋江文学城/蜻蜓FM等',
    video_url   VARCHAR(512) NOT NULL DEFAULT '' COMMENT '视频媒体地址（主播放源，有画面也有声音）',
    audio_url   VARCHAR(512) NOT NULL DEFAULT '' COMMENT '纯音频地址（无视频时回退播放）',
    poster_url  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '播放器封面（为空用分类渐变占位）',
    duration    VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '时长/集数展示，如 12:34 / 全 12 集',
    status      TINYINT      NOT NULL DEFAULT 1  COMMENT '0 下架，1 发布',
    views       INT          NOT NULL DEFAULT 0  COMMENT '浏览量',
    likes       INT          NOT NULL DEFAULT 0  COMMENT '点赞数',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0  COMMENT '逻辑删除：0 正常，1 已删除',
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_status_ctime (status, create_time),
    KEY idx_status_views (status, views)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='广播剧内容表（首页广播剧）';

-- =========================================================
-- 种子数据（仅首次执行插入，重复执行被 IGNORE 跳过）
-- =========================================================

-- 分类字典
INSERT IGNORE INTO t_drama_category (id, code, name, icon, sort_no, status) VALUES
    (1, 'suspense', '悬疑', 'suspense', 1, 1),
    (2, 'romance',  '言情', 'romance',  2, 1),
    (3, 'sci-fi',   '科幻', 'sci-fi',   3, 1),
    (4, 'healing',  '治愈', 'healing',  4, 1),
    (5, 'ancient',  '古风', 'ancient',  5, 1),
    (6, 'children', '儿童', 'children', 6, 1);

-- 广播剧（video_url 为公开示例视频，仅作演示占位）
SET @base = 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/';
INSERT IGNORE INTO t_drama
    (id, title, summary, cover_url, category, tags, platform, video_url, audio_url, poster_url, duration, status, views, likes, create_time)
VALUES
    (1, '雾港谜案', '潮水退去后，码头石阶上多了一枚没有主人的旧怀表。',
     '', '悬疑', '悬疑,推理,怀表', '喜马拉雅', CONCAT(@base, 'BigBuckBunny.mp4'), '', '', '全 8 集', 1, 821, 142, '2026-09-20 20:00:00'),
    (2, '晚风寄来的情书', '一封没有邮票的信，在三个人的抽屉里辗转了十年。',
     '', '言情', '言情,信,十年', '猫耳FM', CONCAT(@base, 'ElephantsDream.mp4'), '', '', '全 6 集', 1, 654, 98, '2026-09-22 21:30:00'),
    (3, '星轨之外', '最后一艘方舟驶离地球时，她在舷窗上写下一个名字。',
     '', '科幻', '科幻,方舟,末日', '蜻蜓FM', CONCAT(@base, 'ForBiggerBlazes.mp4'), '', '', '12:34', 1, 932, 177, '2026-09-25 19:10:00'),
    (4, '猫咪咖啡馆的午后', '一只不爱说话的猫，治愈了所有推门进来的人。',
     '', '治愈', '治愈,猫咪,咖啡馆', '喜马拉雅', CONCAT(@base, 'ForBiggerEscapes.mp4'), '', '', '全 10 集', 1, 1180, 263, '2026-09-28 15:40:00'),
    (5, '长安月明', '上元灯夜里，一位画师为素未谋面的女子画了一整面墙的月亮。',
     '', '古风', '古风,长安,灯会', '猫耳FM', CONCAT(@base, 'ForBiggerFun.mp4'), '', '', '全 9 集', 1, 743, 151, '2026-10-01 22:05:00'),
    (6, '会发光的小怪兽', '夜里躲在被窝发光的它，其实只是想陪你一起怕黑。',
     '', '儿童', '儿童,怪兽,陪伴', '凯叔讲故事', CONCAT(@base, 'ForBiggerJoyrides.mp4'), '', '', '全 12 集', 1, 560, 119, '2026-10-03 18:20:00'),
    (7, '第七次钟声', '钟楼每响一次，就有一个人忘记今天发生过的事。',
     '', '悬疑', '悬疑,钟楼,失忆', '喜马拉雅', CONCAT(@base, 'ForBiggerMeltdowns.mp4'), '', '', '全 7 集', 1, 689, 133, '2026-10-06 20:45:00'),
    (8, '时间旅人的信', '寄给十年前的自己，却先被十年后的人收下了。',
     '', '科幻', '科幻,时间,信', '蜻蜓FM', CONCAT(@base, 'Sintel.mp4'), '', '', '全 5 集', 1, 877, 190, '2026-10-09 21:15:00'),
    (9, '海边的旧信封', '退潮后沙滩上出现一只漂流瓶，里面是三十年前的告白。',
     '', '治愈', '治愈,海边,漂流瓶', '喜马拉雅', CONCAT(@base, 'TearsOfSteel.mp4'), '', '', '11:20', 1, 1024, 208, '2026-10-12 16:50:00');
