-- =========================================================
-- 广播剧媒体地址修复脚本（幂等，可重复执行）
-- 数据库：dream_login
-- 执行方式：
--   mysql -uroot -p123456 --default-character-set=utf8mb4 dream_login < dream_drama_media_fix.sql
--
-- 目的：让每一部广播剧都拥有「各不相同」的视频地址（video_url），
--       满足「每个故事都有一个不同的视频」的需求。
--
-- 说明：
--   前端「视频画面」会优先播放本剧的角色动画（由各人物专属画面组成，每个故事自然不同）；
--   此处同时把 t_drama.video_url 写成各自独立的文件名，便于将来把真实视频
--   直接放到 springBootTest/media/drama/<文件名> 或
--   src/main/resources/static/media/drama/<文件名> 即可生效。
--
--   本地媒体文件位置：
--     springBootTest/src/main/resources/static/media/drama/   （随 jar 打包）
--     springBootTest/media/drama/                            （运行目录，优先命中）
-- =========================================================

USE dream_login;

SET @base = '/media/drama/';

-- 1. 按 id 精确分配 9 个互不相同的视频文件名（仅当当前值不是目标值才改写，可重复执行）
UPDATE t_drama SET video_url = CONCAT(@base, 'fog-harbor.mp4')      WHERE id = 1 AND video_url <> CONCAT(@base, 'fog-harbor.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'love-letter.mp4')    WHERE id = 2 AND video_url <> CONCAT(@base, 'love-letter.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'star-track.mp4')     WHERE id = 3 AND video_url <> CONCAT(@base, 'star-track.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'cat-cafe.mp4')       WHERE id = 4 AND video_url <> CONCAT(@base, 'cat-cafe.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'changan-moon.mp4')   WHERE id = 5 AND video_url <> CONCAT(@base, 'changan-moon.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'glow-monster.mp4')   WHERE id = 6 AND video_url <> CONCAT(@base, 'glow-monster.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'seventh-bell.mp4')   WHERE id = 7 AND video_url <> CONCAT(@base, 'seventh-bell.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'time-letter.mp4')    WHERE id = 8 AND video_url <> CONCAT(@base, 'time-letter.mp4');
UPDATE t_drama SET video_url = CONCAT(@base, 'seaside-letter.mp4') WHERE id = 9 AND video_url <> CONCAT(@base, 'seaside-letter.mp4');

-- 2. 兜底：其余任何仍指向 Google 示例桶的记录，按 id 改成对应的本地独立文件名
UPDATE t_drama SET video_url = CONCAT(@base, 'fog-harbor.mp4')
WHERE video_url LIKE '%commondatastorage.googleapis.com%' AND (id IS NULL OR id = 1);
UPDATE t_drama SET video_url = CONCAT(@base, 'seventh-bell.mp4')
WHERE video_url LIKE '%commondatastorage.googleapis.com%' AND id = 7;

-- 3. 校验：应返回 0 行（所有视频地址互不相同且为本地地址）
-- SELECT id, title, video_url FROM t_drama
-- WHERE video_url = ''
--    OR video_url LIKE '%commondatastorage.googleapis.com%'
--    OR video_url IN (SELECT video_url FROM t_drama GROUP BY video_url HAVING COUNT(*) > 1);
