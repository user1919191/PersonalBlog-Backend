CREATE TABLE IF NOT EXISTS `user_reading_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `last_anchor` VARCHAR(255) DEFAULT '',
  `last_read_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_user_last_read` (`user_id`, `last_read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户阅读历史';

CREATE TABLE IF NOT EXISTS `user_subscription` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `muted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_target` (`user_id`, `type`, `target_id`),
  KEY `idx_type_target` (`type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅关系';

CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `type` VARCHAR(50) NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `link` VARCHAR(255) DEFAULT '',
  `actor_id` BIGINT NULL,
  `actor_name` VARCHAR(100) DEFAULT '',
  `target_type` VARCHAR(50) DEFAULT '',
  `target_id` BIGINT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_type_time` (`type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知事件';

CREATE TABLE IF NOT EXISTS `user_notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `notification_id` BIGINT NOT NULL,
  `is_read` TINYINT DEFAULT 0,
  `read_at` DATETIME NULL,
  `is_deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_unread` (`user_id`, `is_read`, `is_deleted`, `create_time`),
  KEY `idx_notification` (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知收件箱';
