-- =========================================================
-- 广播剧媒体地址修复脚本（幂等，可重复执行）
-- 数据库：dream_login
-- 执行方式：
--   mysql -uroot -p123456 --default-character-set=utf8mb4 dream_login < dream_drama_media_fix.sql
--
-- 背景：
--   t_drama.video_url 早期填的是 Google 公开示例桶地址
--   （https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/*.mp4），
--   该域名在国内网络不可达，浏览器加载不到视频流 → 播放器黑屏「播放不了」。
--   本脚本将其替换为后端托管的本地媒体地址 /media/drama/*.mp4
--   （后端 WebConfig 已注册 /media/** 静态资源映射，支持 Range 断点续传）。
--
--   本地示例媒体文件位置：
--     springBootTest/src/main/resources/static/media/drama/   （随 jar 打包）
--     springBootTest/media/drama/                            （运行目录，优先命中，放真实视频无需重新打包）
-- =========================================================

USE dream_login;

-- 1. 按 id 精确修复 9 条种子数据（仅当原地址为空或仍指向 Google 时才改写）
UPDATE t_drama SET video_url = '/media/drama/bunny-trailer.mp4'
WHERE id = 1  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/movie-300.mp4'
WHERE id = 2  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/flower.mp4'
WHERE id = 3  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/sintel-trailer.mp4'
WHERE id = 4  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/friday.mp4'
WHERE id = 5  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/flower.mp4'
WHERE id = 6  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/sintel-trailer.mp4'
WHERE id = 7  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/sintel-trailer.mp4'
WHERE id = 8  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

UPDATE t_drama SET video_url = '/media/drama/movie-300.mp4'
WHERE id = 9  AND (video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%');

-- 2. 兜底：其余任何仍指向 Google 示例桶的记录统一改成可用的本地媒体
UPDATE t_drama SET video_url = '/media/drama/sintel-trailer.mp4'
WHERE video_url LIKE '%commondatastorage.googleapis.com%';

-- 2.5 movie-300.mp4 是带计时器的测试画面，与小说内容无关，已下线该文件并改指其它素材
UPDATE t_drama SET video_url = '/media/drama/sintel-trailer.mp4'
WHERE id = 2 AND video_url = '/media/drama/movie-300.mp4';

UPDATE t_drama SET video_url = '/media/drama/bunny-trailer.mp4'
WHERE id = 9 AND video_url = '/media/drama/movie-300.mp4';

UPDATE t_drama SET video_url = '/media/drama/sintel-trailer.mp4'
WHERE video_url = '/media/drama/movie-300.mp4';

-- 3. 校验：应返回 0 行
-- SELECT id, title, video_url FROM t_drama
-- WHERE video_url = '' OR video_url LIKE '%commondatastorage.googleapis.com%';
