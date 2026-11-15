-- ============================================================
-- 个人博客系统 - 完整数据库初始化脚本（整合版）
-- Database: blog_system
-- Charset : utf8mb4
-- 说明：直接执行本文件即可创建数据库、全部表结构与默认管理员账号。
-- 默认管理员：admin / admin123
-- ============================================================
-- Blog database initialization SQL
-- Database: blog_system
-- Charset: utf8mb4
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS blog_system
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE blog_system;

CREATE TABLE IF NOT EXISTS `user` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `email`          VARCHAR(100)          UNIQUE,
  `username`       VARCHAR(50)  NOT NULL UNIQUE,
  `password`       VARCHAR(255),
  `avatar`         VARCHAR(255),
  `role`           VARCHAR(20)  NOT NULL DEFAULT 'USER',
  `status`         TINYINT      NOT NULL DEFAULT 1,
  `gitee_id`       VARCHAR(50)           UNIQUE,
  `github_id`      VARCHAR(50)           UNIQUE,
  `oauth_provider` VARCHAR(20),
  `display_name`   VARCHAR(50),
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_email`     (`email`),
  INDEX `idx_username`  (`username`),
  INDEX `idx_gitee_id`  (`gitee_id`),
  INDEX `idx_github_id` (`github_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50) NOT NULL UNIQUE,
  `description` VARCHAR(200),
  `sort_order`  INT         NOT NULL DEFAULT 0,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `article_series` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL,
  `description` VARCHAR(500),
  `cover_image` VARCHAR(255),
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `article` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `title`         VARCHAR(200) NOT NULL,
  `content`       LONGTEXT     NOT NULL,
  `summary`       VARCHAR(500),
  `cover_image`   VARCHAR(255),
  `category_id`   BIGINT       NOT NULL,
  `author_id`     BIGINT       NOT NULL,
  `series_id`     BIGINT,
  `series_order`  INT          NOT NULL DEFAULT 0,
  `view_count`    INT          NOT NULL DEFAULT 0,
  `like_count`    INT          NOT NULL DEFAULT 0,
  `comment_count` INT          NOT NULL DEFAULT 0,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `visibility`    VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
  `is_top`        TINYINT      NOT NULL DEFAULT 0,
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_category`    (`category_id`),
  INDEX `idx_author`      (`author_id`),
  INDEX `idx_series`      (`series_id`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_status`      (`status`),
  INDEX `idx_status_visibility_create_time` (`status`, `visibility`, `create_time`),
  FULLTEXT `idx_fulltext`(`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tag` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(50) NOT NULL UNIQUE,
  `article_count` INT         NOT NULL DEFAULT 0,
  `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `article_tag` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL,
  `tag_id`     BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
  INDEX `idx_article_id` (`article_id`),
  INDEX `idx_tag_id`     (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `article_id`  BIGINT   NOT NULL,
  `parent_id`   BIGINT   NOT NULL DEFAULT 0,
  `user_id`     BIGINT,
  `content`     TEXT     NOT NULL,
  `nickname`    VARCHAR(50),
  `email`       VARCHAR(100),
  `like_count`  INT      NOT NULL DEFAULT 0,
  `status`      TINYINT  NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_article` (`article_id`),
  INDEX `idx_parent`  (`parent_id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `article_like` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `article_id`  BIGINT   NOT NULL,
  `user_id`     BIGINT   NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `featured_article` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `title`       VARCHAR(200) NOT NULL,
  `url`         VARCHAR(500) NOT NULL,
  `category`    VARCHAR(50)  NOT NULL,
  `description` VARCHAR(500),
  `cover_image` VARCHAR(255),
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `interview` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `title`         VARCHAR(200) NOT NULL,
  `content`       LONGTEXT     NOT NULL,
  `summary`       VARCHAR(500),
  `cover_image`   VARCHAR(255),
  `category_id`   BIGINT       NOT NULL,
  `author_id`     BIGINT       NOT NULL,
  `view_count`    INT          NOT NULL DEFAULT 0,
  `like_count`    INT          NOT NULL DEFAULT 0,
  `comment_count` INT          NOT NULL DEFAULT 0,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `visibility`    VARCHAR(20)  NOT NULL DEFAULT 'PUBLIC',
  `is_top`        TINYINT      NOT NULL DEFAULT 0,
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_category`    (`category_id`),
  INDEX `idx_status`      (`status`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_status_visibility_create_time` (`status`, `visibility`, `create_time`),
  FULLTEXT `idx_fulltext` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `interview_comment` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `interview_id` BIGINT   NOT NULL,
  `parent_id`    BIGINT   NOT NULL DEFAULT 0,
  `user_id`      BIGINT,
  `content`      TEXT     NOT NULL,
  `nickname`     VARCHAR(50),
  `email`        VARCHAR(100),
  `like_count`   INT      NOT NULL DEFAULT 0,
  `status`       TINYINT  NOT NULL DEFAULT 1,
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_interview` (`interview_id`),
  INDEX `idx_user_id`   (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `forum_post` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `title`           VARCHAR(200) NOT NULL,
  `content`         TEXT         NOT NULL,
  `user_id`         BIGINT       NOT NULL,
  `view_count`      INT          NOT NULL DEFAULT 0,
  `reply_count`     INT          NOT NULL DEFAULT 0,
  `last_reply_time` DATETIME,
  `is_top`          TINYINT      NOT NULL DEFAULT 0,
  `status`          TINYINT      NOT NULL DEFAULT 1,
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user`       (`user_id`),
  INDEX `idx_last_reply` (`last_reply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `forum_reply` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `post_id`     BIGINT   NOT NULL,
  `user_id`     BIGINT   NOT NULL,
  `content`     TEXT     NOT NULL,
  `parent_id`   BIGINT   NOT NULL DEFAULT 0,
  `status`      TINYINT  NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_post` (`post_id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `website_nav` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL,
  `url`         VARCHAR(500) NOT NULL,
  `description` VARCHAR(500),
  `category`    VARCHAR(50)  NOT NULL,
  `logo`        VARCHAR(255),
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `favorite_folder` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `name`        VARCHAR(50) NOT NULL,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `favorite_item` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `folder_id`   BIGINT      NOT NULL,
  `item_type`   VARCHAR(20) NOT NULL,
  `item_id`     BIGINT      NOT NULL,
  `learning_status` VARCHAR(20)  NOT NULL DEFAULT 'UNREAD',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_folder_item` (`folder_id`, `item_type`, `item_id`),
  INDEX `idx_folder_id` (`folder_id`),
  INDEX `idx_item`      (`item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_reading_history` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `article_id`  BIGINT       NOT NULL,
  `last_anchor` VARCHAR(255)          DEFAULT '',
  `last_read_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  INDEX `idx_user_last_read` (`user_id`, `last_read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_subscription` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `type`        VARCHAR(20) NOT NULL,
  `target_id`   BIGINT      NOT NULL,
  `muted`       TINYINT     NOT NULL DEFAULT 0,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `type`, `target_id`),
  INDEX `idx_type_target` (`type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `notification` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `type`        VARCHAR(50)  NOT NULL,
  `title`       VARCHAR(200) NOT NULL,
  `content`     VARCHAR(500) NOT NULL,
  `link`        VARCHAR(255)          DEFAULT '',
  `actor_id`    BIGINT                DEFAULT NULL,
  `actor_name`  VARCHAR(100)          DEFAULT '',
  `target_type` VARCHAR(50)           DEFAULT '',
  `target_id`   BIGINT                DEFAULT NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_type_time` (`type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_notification` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT   NOT NULL,
  `notification_id` BIGINT   NOT NULL,
  `is_read`         TINYINT  NOT NULL DEFAULT 0,
  `read_at`         DATETIME          DEFAULT NULL,
  `is_deleted`      TINYINT  NOT NULL DEFAULT 0,
  `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_unread` (`user_id`, `is_read`, `is_deleted`, `create_time`),
  INDEX `idx_notification` (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `visit_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT,
  `ip`          VARCHAR(50)  NOT NULL,
  `url`         VARCHAR(500) NOT NULL,
  `user_agent`  VARCHAR(500),
  `referer`     VARCHAR(500),
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_url`         (`url`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `daily_stats` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT,
  `stat_date`   DATE   NOT NULL UNIQUE,
  `pv`          BIGINT NOT NULL DEFAULT 0,
  `uv`          BIGINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO `user` (`email`, `username`, `password`, `role`) VALUES
  ('admin@blog.com',       'admin',       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN');

-- 默认分类
INSERT IGNORE INTO `category` (`name`, `description`, `sort_order`) VALUES
  ('后端开发', 'Java、Spring 等技术文章', 1),
  ('前端开发', 'Vue、CSS 等前端技术', 2),
  ('数据库', 'MySQL、Redis 等数据存储', 3);


SELECT 'Database initialization completed, 22 tables created.' AS result;
