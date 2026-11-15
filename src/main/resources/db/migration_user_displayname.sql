-- 补充 user 表 display_name 字段（若已存在请跳过此步骤）
ALTER TABLE `user`
  ADD COLUMN `display_name` VARCHAR(50) DEFAULT NULL COMMENT '显示名称（评论展示用）';
