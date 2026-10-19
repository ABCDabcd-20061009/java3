-- =========================================================
-- 粉金梦境 登录注册系统 数据库脚本（建库 + 用户表 + 演示账号）
-- 数据库：dream_login    字符集：utf8mb4
-- 本脚本幂等：可重复执行不会清空已有账号
--   （如需彻底重建，手动取消下面 DROP TABLE 注释后再执行）
-- 使用方式（任选其一）：
--   1) 命令行：mysql -uroot -p123456 --default-character-set=utf8mb4 < dream_login.sql
--   2) 在 IDEA / Navicat / DataGrip 中直接执行本文件
-- 执行顺序：本脚本 → dream_home.sql（首页 4 张表）
-- =========================================================

CREATE DATABASE IF NOT EXISTS dream_login
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE dream_login;

-- ---------------------------------------------------------
-- 用户表
-- ---------------------------------------------------------
-- DROP TABLE IF EXISTS t_user;   -- 需要清库重建时才打开本行

CREATE TABLE IF NOT EXISTS t_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(20)  NOT NULL COMMENT '用户名',
    password    VARCHAR(64)  NOT NULL COMMENT '密码（SHA-256 加盐哈希）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT ='用户表';

-- ---------------------------------------------------------
-- 演示账号（密码统一为 abc123456，与 PasswordUtil 的 SHA-256 加盐规则一致）
-- id 与 dream_home.sql 里 t_dream 种子数据的作者 user_id 对齐
-- ---------------------------------------------------------
INSERT IGNORE INTO t_user (id, username, password) VALUES
    (1, '梦梦', '85f35311f18e22cf5e714a674a1685914ad47468ac01de88d9fc9bed816f42b1'),
    (2, '星星', '85f35311f18e22cf5e714a674a1685914ad47468ac01de88d9fc9bed816f42b1'),
    (3, '夜风', '85f35311f18e22cf5e714a674a1685914ad47468ac01de88d9fc9bed816f42b1');
