-- ============================================================
-- 博客系统全量迁移脚本（新功能补丁）
-- 涵盖：文章专栏、用户显示名、日程待办、论坛
-- 适用：MySQL 8.x
-- 执行方式：在 blog_system 数据库下完整运行此文件
-- 注意：本脚本不使用 DELIMITER，可直接在 Navicat / DBeaver 中运行
-- ============================================================

USE blog_system;

-- ============================================================
-- 1. user 表：补充 display_name 列
-- ============================================================
SET @v = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'user'
    AND COLUMN_NAME  = 'display_name'
);
SET @sql = IF(@v > 0,
  'SELECT ''display_name already exists'' AS info',
  'ALTER TABLE `user` ADD COLUMN `display_name` VARCHAR(50) DEFAULT NULL COMMENT ''显示名称（评论展示用）'''
);
PREPARE _stmt FROM @sql; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;


-- ============================================================
-- 2. 文章专栏表（已有则跳过）
-- ============================================================
CREATE TABLE IF NOT EXISTS `article_series` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL COMMENT '专栏名称',
  `description` VARCHAR(500)          COMMENT '专栏介绍',
  `cover_image` VARCHAR(255)          COMMENT '封面图',
  `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章专栏表';


-- ============================================================
-- 3. article 表：补充专栏相关列
-- ============================================================
SET @v = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'article'
    AND COLUMN_NAME  = 'series_id'
);
SET @sql = IF(@v > 0,
  'SELECT ''series_id already exists'' AS info',
  'ALTER TABLE `article` ADD COLUMN `series_id` BIGINT DEFAULT NULL COMMENT ''所属专栏ID'''
);
PREPARE _stmt FROM @sql; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @v = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'article'
    AND COLUMN_NAME  = 'series_order'
);
SET @sql = IF(@v > 0,
  'SELECT ''series_order already exists'' AS info',
  'ALTER TABLE `article` ADD COLUMN `series_order` INT DEFAULT 0 COMMENT ''在专栏中的顺序'''
);
PREPARE _stmt FROM @sql; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;

SET @v = (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'article'
    AND INDEX_NAME   = 'idx_series'
);
SET @sql = IF(@v > 0,
  'SELECT ''idx_series already exists'' AS info',
  'ALTER TABLE `article` ADD INDEX `idx_series` (`series_id`)'
);
PREPARE _stmt FROM @sql; EXECUTE _stmt; DEALLOCATE PREPARE _stmt;


-- ============================================================
-- 4. 论坛帖子表（已有则跳过）
-- ============================================================
CREATE TABLE IF NOT EXISTS `forum_post` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `title`           VARCHAR(200) NOT NULL COMMENT '标题',
  `content`         TEXT         NOT NULL COMMENT '内容',
  `user_id`         BIGINT       NOT NULL COMMENT '发帖人ID',
  `view_count`      INT          NOT NULL DEFAULT 0 COMMENT '浏览量',
  `reply_count`     INT          NOT NULL DEFAULT 0 COMMENT '回复数',
  `last_reply_time` DATETIME              COMMENT '最后回复时间',
  `is_top`          TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶: 1是 0否',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_last_reply` (`last_reply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';


-- ============================================================
-- 5. 论坛回复表（已有则跳过）
-- ============================================================
CREATE TABLE IF NOT EXISTS `forum_reply` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
  `user_id`     BIGINT   NOT NULL COMMENT '回复人ID',
  `content`     TEXT     NOT NULL COMMENT '回复内容',
  `parent_id`   BIGINT   NOT NULL DEFAULT 0 COMMENT '父回复ID（0=楼层回复）',
  `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_post` (`post_id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛回复表';


-- ============================================================
-- 6. 日程待办表（已有则跳过）
-- ============================================================
CREATE TABLE IF NOT EXISTS `todo` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT        NOT NULL COMMENT '所属用户',
  `title`          VARCHAR(200)  NOT NULL COMMENT '待办标题',
  `description`    VARCHAR(1000)          COMMENT '备注说明',
  `priority`       VARCHAR(10)   NOT NULL DEFAULT 'MEDIUM' COMMENT 'LOW/MEDIUM/HIGH',
  `status`         VARCHAR(10)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE',
  `scheduled_date` DATE          NOT NULL COMMENT '计划日期',
  `completed_at`   DATETIME               COMMENT '完成时间',
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_date` (`user_id`, `scheduled_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日程待办';


SELECT '迁移完成！' AS result;
