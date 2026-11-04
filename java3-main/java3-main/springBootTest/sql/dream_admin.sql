-- ============================================================
-- 后台管理员表 t_admin（幂等脚本，可重复执行）
-- 与 config/AdminSchemaInit.java 等价；正常情况启动时自动建表植入，无需手工执行。
-- 默认账号：admin / admin123（密码为 PasswordUtil（SHA-256 加盐 dream-login@2026）的哈希）
-- ============================================================

CREATE TABLE IF NOT EXISTS t_admin (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL COMMENT '管理员用户名',
  password VARCHAR(128) NOT NULL COMMENT '密码（SHA-256 加盐）',
  create_time DATETIME NULL,
  update_time DATETIME NULL,
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
  UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员';

INSERT INTO t_admin (username, password, create_time, update_time)
SELECT 'admin', '2609b3b250f1dbb65f2281f740da64a241438bb48be3829249cdb6f462d00528', NOW(), NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_admin WHERE username = 'admin');
-- 上面的哈希即 admin123 + 盐 dream-login@2026 的 SHA-256（与 util/PasswordUtil 一致）。
