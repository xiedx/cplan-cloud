-- ============================================================
-- cplan_creation schema — Creation Service database
-- ============================================================

CREATE DATABASE IF NOT EXISTS cplan_creation
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE cplan_creation;

-- Project table
CREATE TABLE t_project (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT          NOT NULL                COMMENT '所属用户 ID',
    title           VARCHAR(256)    NOT NULL                COMMENT '项目标题',
    description     VARCHAR(1024)   DEFAULT NULL            COMMENT '项目描述',
    status          TINYINT         DEFAULT 0               COMMENT '状态: 0=草稿, 1=生成中, 2=已完成, 3=失败',
    final_video_url VARCHAR(1024)   DEFAULT NULL            COMMENT '最终视频 URL',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted      TINYINT         DEFAULT 0               COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- Outline table
CREATE TABLE t_outline (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    project_id       BIGINT          NOT NULL                COMMENT '所属项目 ID',
    content          TEXT                                    COMMENT '用户输入的大纲原文',
    enriched_content TEXT                                    COMMENT 'AI 补全后的大纲',
    status           TINYINT         DEFAULT 0               COMMENT '状态: 0=待处理, 1=处理中, 2=已完成',
    created_at       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT         DEFAULT 0               COMMENT '逻辑删除',
    INDEX idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大纲表';

-- Script table
CREATE TABLE t_script (
    id          BIGINT          AUTO_INCREMENT PRIMARY KEY,
    project_id  BIGINT          NOT NULL                    COMMENT '所属项目 ID',
    outline_id  BIGINT          DEFAULT NULL                COMMENT '关联大纲 ID',
    content     MEDIUMTEXT                                  COMMENT '完整剧本文本',
    llm_model   VARCHAR(64)     DEFAULT NULL                COMMENT '使用的 LLM 模型',
    status      TINYINT         DEFAULT 0                   COMMENT '状态: 0=生成中, 1=已完成, 2=失败',
    created_at  DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    updated_at  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted  TINYINT         DEFAULT 0                   COMMENT '逻辑删除',
    INDEX idx_project_id (project_id),
    INDEX idx_outline_id (outline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='剧本表';

-- Storyboard table
CREATE TABLE t_storyboard (
    id                BIGINT          AUTO_INCREMENT PRIMARY KEY,
    project_id        BIGINT          NOT NULL              COMMENT '所属项目 ID',
    script_id         BIGINT          DEFAULT NULL          COMMENT '关联剧本 ID',
    sequence_no       INT             NOT NULL              COMMENT '分镜序号',
    scene_description TEXT            DEFAULT NULL          COMMENT '场景描述',
    dialogue          TEXT            DEFAULT NULL          COMMENT '台词',
    image_prompt      VARCHAR(2048)   DEFAULT NULL          COMMENT '文生图 Prompt',
    video_prompt      VARCHAR(2048)   DEFAULT NULL          COMMENT '文生视频 Prompt',
    status            TINYINT         DEFAULT 0             COMMENT '状态: 0=待审核, 1=已审核, 2=视频生成中, 3=视频完成, 4=失败',
    video_url         VARCHAR(1024)   DEFAULT NULL          COMMENT '生成的视频片段地址',
    created_at        DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at        DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted        TINYINT         DEFAULT 0             COMMENT '逻辑删除',
    INDEX idx_project_id (project_id),
    INDEX idx_script_id (script_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分镜表';

-- Video Task table
CREATE TABLE t_video_task (
    id               BIGINT          AUTO_INCREMENT PRIMARY KEY,
    project_id       BIGINT          NOT NULL                COMMENT '所属项目 ID',
    storyboard_id    BIGINT          DEFAULT NULL            COMMENT '关联分镜 ID（合成任务可为空）',
    task_type        VARCHAR(32)     NOT NULL                COMMENT '任务类型: SCRIPT_GEN, VIDEO_GEN, COMPOSE',
    task_status      VARCHAR(16)     DEFAULT 'PENDING'       COMMENT '任务状态: PENDING, PROCESSING, SUCCESS, FAILED, RETRY',
    external_task_id VARCHAR(128)    DEFAULT NULL            COMMENT '外部 AI 平台任务 ID',
    error_message    TEXT            DEFAULT NULL            COMMENT '错误信息',
    retry_count      INT             DEFAULT 0               COMMENT '重试次数',
    started_at       DATETIME        DEFAULT NULL            COMMENT '开始时间',
    finished_at      DATETIME        DEFAULT NULL            COMMENT '完成时间',
    created_at       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted       TINYINT         DEFAULT 0               COMMENT '逻辑删除',
    INDEX idx_project_id (project_id),
    INDEX idx_storyboard_id (storyboard_id),
    INDEX idx_task_type (task_type),
    INDEX idx_task_status (task_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频任务表';

-- Video Segment table
CREATE TABLE t_video_segment (
    id            BIGINT          AUTO_INCREMENT PRIMARY KEY,
    project_id    BIGINT          NOT NULL                    COMMENT '所属项目 ID',
    storyboard_id BIGINT          NOT NULL                    COMMENT '关联分镜 ID',
    file_key      VARCHAR(512)    NOT NULL                    COMMENT 'MinIO 对象 Key',
    file_url      VARCHAR(1024)   DEFAULT NULL                COMMENT '访问 URL',
    duration_ms   INT             DEFAULT NULL                COMMENT '时长（毫秒）',
    file_size     BIGINT          DEFAULT NULL                COMMENT '文件大小（字节）',
    status        TINYINT         DEFAULT 0                   COMMENT '状态: 0=上传中, 1=可用, 2=删除',
    created_at    DATETIME        DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
    is_deleted    TINYINT         DEFAULT 0                   COMMENT '逻辑删除',
    INDEX idx_project_id (project_id),
    INDEX idx_storyboard_id (storyboard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频片段表';
