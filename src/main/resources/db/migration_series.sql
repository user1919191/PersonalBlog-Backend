-- 文章专栏功能迁移脚本
-- 执行前请先备份数据库

-- 1. 创建专栏表
CREATE TABLE IF NOT EXISTS `article_series` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '专栏名称',
  `description` VARCHAR(500) COMMENT '专栏介绍',
  `cover_image` VARCHAR(255) COMMENT '封面图',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sort (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章专栏表';

-- 2. 为文章表添加专栏相关字段（若已有这两列请跳过此步骤）
ALTER TABLE `article`
  ADD COLUMN `series_id` BIGINT DEFAULT NULL COMMENT '所属专栏ID',
  ADD COLUMN `series_order` INT DEFAULT 0 COMMENT '在专栏中的顺序',
  ADD INDEX idx_series (`series_id`);
