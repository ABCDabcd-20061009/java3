-- =========================================================
-- 粉金梦境 登录注册系统 数据库脚本
-- 数据库：dream_login    字符集：utf8mb4
-- 使用方式（任选其一）：
--   1) 命令行：mysql -uroot -p123456 < dream_login.sql
--   2) 在 IDEA / Navicat / DataGrip 中直接执行本文件
-- =========================================================

CREATE DATABASE IF NOT EXISTS dream_login
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE dream_login;

-- ---------------------------------------------------------
-- 用户表
-- ---------------------------------------------------------
DROP TABLE IF EXISTS t_user;

CREATE TABLE t_user (
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

-- 测试账号可自行通过注册接口创建
