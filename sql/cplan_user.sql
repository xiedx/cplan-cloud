-- ============================================================
-- cplan_user schema — User Service database
-- ============================================================

CREATE DATABASE IF NOT EXISTS cplan_user
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE cplan_user;

-- User table
CREATE TABLE t_user (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(64)     NOT NULL                COMMENT '用户名',
    email           VARCHAR(128)    NOT NULL                COMMENT '邮箱',
    password_hash   VARCHAR(256)    NOT NULL                COMMENT 'BCrypt 密码哈希',
    avatar_url      VARCHAR(512)    DEFAULT NULL            COMMENT '头像 URL',
    status          TINYINT         DEFAULT 1               COMMENT '状态: 0=禁用, 1=正常',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT         DEFAULT 0               COMMENT '逻辑删除: 0=未删除, 1=已删除',
    UNIQUE KEY uk_email (email),
    INDEX idx_username (username),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
