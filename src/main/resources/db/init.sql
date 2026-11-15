-- =============================================
-- 博客系统生产环境数据库初始化脚本
-- 生成日期：2026-03-14
-- 来源文件：schema.sql + alter_user_table.sql + user_favorites_migration.sql
-- 说明：已将三份迁移文件合并为一份，移除全部测试数据
--       仅保留管理员账号、分类、网站导航（30条）、精选文章（83条）
-- =============================================

CREATE DATABASE IF NOT EXISTS blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog;

-- =============================================
-- 建表
-- =============================================

-- 1. 用户表（已合并 display_name、gitee_id、oauth_provider 字段）
CREATE TABLE `user` (
  `id`             BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `email`          VARCHAR(100) UNIQUE COMMENT '邮箱',
  `username`       VARCHAR(50)  UNIQUE NOT NULL COMMENT '用户名',
  `password`       VARCHAR(255) COMMENT '密码（OAuth用户可为空）',
  `avatar`         VARCHAR(255) COMMENT '头像URL',
  `role`           VARCHAR(20)  DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
  `status`         TINYINT      DEFAULT 1 COMMENT '状态: 1正常 0禁用',
  `gitee_id`       VARCHAR(50)  UNIQUE COMMENT 'Gitee用户ID',
  `oauth_provider` VARCHAR(20)  COMMENT 'OAuth提供商: gitee/github',
  `display_name`   VARCHAR(50)  COMMENT '显示名称（用于评论展示）',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email        (`email`),
  INDEX idx_username     (`username`),
  INDEX idx_gitee_id     (`gitee_id`),
  INDEX idx_display_name (`display_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 分类表
CREATE TABLE `category` (
  `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL UNIQUE COMMENT '分类名称',
  `description` VARCHAR(200) COMMENT '分类描述',
  `sort_order`  INT          DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 3. 文章表
CREATE TABLE `article` (
  `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `title`         VARCHAR(200) NOT NULL COMMENT '标题',
  `content`       LONGTEXT     NOT NULL COMMENT 'Markdown内容',
  `summary`       VARCHAR(500) COMMENT '摘要',
  `cover_image`   VARCHAR(255) COMMENT '封面图',
  `category_id`   BIGINT       NOT NULL COMMENT '分类ID',
  `author_id`     BIGINT       NOT NULL COMMENT '作者ID',
  `view_count`    INT          DEFAULT 0 COMMENT '浏览量',
  `like_count`    INT          DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT          DEFAULT 0 COMMENT '评论数',
  `status`        TINYINT      DEFAULT 1 COMMENT '状态: 1发布 0草稿',
  `is_top`        TINYINT      DEFAULT 0 COMMENT '是否置顶',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category    (`category_id`),
  INDEX idx_author      (`author_id`),
  INDEX idx_create_time (`create_time`),
  INDEX idx_status      (`status`),
  FULLTEXT idx_title_content (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 4. 评论表（已合并 user_id 字段；nickname/email 改为可空兼容 OAuth 用户）
CREATE TABLE `comment` (
  `id`          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `article_id`  BIGINT   NOT NULL COMMENT '文章ID',
  `parent_id`   BIGINT   DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
  `content`     TEXT     NOT NULL COMMENT '评论内容',
  `user_id`     BIGINT   COMMENT '用户ID（登录用户）',
  `nickname`    VARCHAR(50)  COMMENT '昵称（历史数据兼容）',
  `email`       VARCHAR(100) COMMENT '邮箱（历史数据兼容）',
  `like_count`  INT      DEFAULT 0 COMMENT '点赞数',
  `status`      TINYINT  DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_article (`article_id`),
  INDEX idx_parent  (`parent_id`),
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 5. 文章点赞表
CREATE TABLE `article_like` (
  `id`          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `article_id`  BIGINT   NOT NULL COMMENT '文章ID',
  `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_article_user (`article_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- 6. 网站导航表
CREATE TABLE `website_nav` (
  `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL COMMENT '网站名称',
  `url`         VARCHAR(500) NOT NULL COMMENT '网站URL',
  `description` VARCHAR(500) COMMENT '网站介绍',
  `category`    VARCHAR(50)  NOT NULL COMMENT '分类',
  `logo`        VARCHAR(255) COMMENT '网站Logo',
  `sort_order`  INT          DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站导航表';

-- 7. 论坛帖子表
CREATE TABLE `forum_post` (
  `id`              BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `title`           VARCHAR(200) NOT NULL COMMENT '标题',
  `content`         TEXT         NOT NULL COMMENT '内容',
  `user_id`         BIGINT       NOT NULL COMMENT '发帖人ID',
  `view_count`      INT          DEFAULT 0 COMMENT '浏览量',
  `reply_count`     INT          DEFAULT 0 COMMENT '回复数',
  `last_reply_time` DATETIME     COMMENT '最后回复时间',
  `is_top`          TINYINT      DEFAULT 0 COMMENT '是否置顶',
  `status`          TINYINT      DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user       (`user_id`),
  INDEX idx_last_reply (`last_reply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- 8. 论坛回复表
CREATE TABLE `forum_reply` (
  `id`          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `post_id`     BIGINT   NOT NULL COMMENT '帖子ID',
  `user_id`     BIGINT   NOT NULL COMMENT '回复人ID',
  `content`     TEXT     NOT NULL COMMENT '回复内容',
  `parent_id`   BIGINT   DEFAULT 0 COMMENT '父回复ID',
  `status`      TINYINT  DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_post (`post_id`),
  INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛回复表';

-- 9. 精选文章表
CREATE TABLE `featured_article` (
  `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `title`       VARCHAR(200) NOT NULL COMMENT '文章标题',
  `url`         VARCHAR(500) NOT NULL COMMENT '文章URL',
  `category`    VARCHAR(50)  NOT NULL COMMENT '类别',
  `description` VARCHAR(500) COMMENT '描述',
  `cover_image` VARCHAR(255) COMMENT '封面图',
  `sort_order`  INT          DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='精选文章表';

-- 10. 访问日志表
CREATE TABLE `visit_log` (
  `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT       COMMENT '用户ID(未登录为NULL)',
  `ip`          VARCHAR(50)  NOT NULL COMMENT 'IP地址',
  `url`         VARCHAR(500) NOT NULL COMMENT '访问URL',
  `user_agent`  VARCHAR(500) COMMENT 'User Agent',
  `referer`     VARCHAR(500) COMMENT '来源',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_create_time (`create_time`),
  INDEX idx_url         (`url`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问日志表';

-- 11. 每日统计表（由定时任务写入，替代明细日志）
CREATE TABLE `daily_stats` (
  `id`          BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `stat_date`   DATE     NOT NULL UNIQUE COMMENT '统计日期',
  `pv`          BIGINT   DEFAULT 0 COMMENT '页面访问量',
  `uv`          BIGINT   DEFAULT 0 COMMENT '独立访客数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_stat_date (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日访问统计表';

-- 12. 面经表
CREATE TABLE `interview` (
  `id`            BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `title`         VARCHAR(200) NOT NULL COMMENT '标题',
  `content`       LONGTEXT     NOT NULL COMMENT 'Markdown内容',
  `summary`       VARCHAR(500) COMMENT '摘要',
  `cover_image`   VARCHAR(255) COMMENT '封面图',
  `category_id`   BIGINT       NOT NULL COMMENT '分类ID',
  `author_id`     BIGINT       NOT NULL COMMENT '作者ID',
  `view_count`    INT          DEFAULT 0 COMMENT '浏览量',
  `like_count`    INT          DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT          DEFAULT 0 COMMENT '评论数',
  `status`        TINYINT      DEFAULT 1 COMMENT '状态: 1发布 0草稿',
  `is_top`        TINYINT      DEFAULT 0 COMMENT '是否置顶',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category    (`category_id`),
  INDEX idx_status      (`status`),
  INDEX idx_create_time (`create_time`),
  FULLTEXT idx_title_content (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面经表';

-- 13. 面经评论表（已合并 user_id 字段；nickname/email 改为可空兼容 OAuth 用户）
CREATE TABLE `interview_comment` (
  `id`           BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `interview_id` BIGINT   NOT NULL COMMENT '面经ID',
  `parent_id`    BIGINT   DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
  `content`      TEXT     NOT NULL COMMENT '评论内容',
  `user_id`      BIGINT   COMMENT '用户ID（登录用户）',
  `nickname`     VARCHAR(50)  COMMENT '昵称（历史数据兼容）',
  `email`        VARCHAR(100) COMMENT '邮箱（历史数据兼容）',
  `like_count`   INT      DEFAULT 0 COMMENT '点赞数',
  `status`       TINYINT  DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time`  DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_interview (`interview_id`),
  INDEX idx_user_id   (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面经评论表';

-- 14. 收藏夹表
CREATE TABLE `favorite_folder` (
  `id`          BIGINT      PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `name`        VARCHAR(50) NOT NULL COMMENT '收藏夹名称',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹表';

-- 15. 收藏项表
CREATE TABLE `favorite_item` (
  `id`          BIGINT      PRIMARY KEY AUTO_INCREMENT,
  `folder_id`   BIGINT      NOT NULL COMMENT '收藏夹ID',
  `item_type`   VARCHAR(20) NOT NULL COMMENT '类型: article/featured/interview',
  `item_id`     BIGINT      NOT NULL COMMENT '内容ID',
  `learning_status` VARCHAR(20) NOT NULL DEFAULT 'UNREAD' COMMENT 'UNREAD/LEARNING/MASTERED',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_folder_id (`folder_id`),
  INDEX idx_item      (`item_type`, `item_id`),
  UNIQUE KEY uk_folder_item (`folder_id`, `item_type`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏项表';

CREATE TABLE `user_reading_history` (
  `id`           BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL COMMENT '用户ID',
  `article_id`   BIGINT       NOT NULL COMMENT '文章ID',
  `last_anchor`  VARCHAR(255) DEFAULT '' COMMENT '最后阅读锚点',
  `last_read_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '最后阅读时间',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_article (`user_id`, `article_id`),
  INDEX idx_user_last_read (`user_id`, `last_read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户阅读历史';

CREATE TABLE `user_subscription` (
  `id`          BIGINT      PRIMARY KEY AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `type`        VARCHAR(20) NOT NULL COMMENT '订阅类型: tag/author',
  `target_id`   BIGINT      NOT NULL COMMENT '订阅目标ID',
  `muted`       TINYINT     DEFAULT 0 COMMENT '是否静默',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_target (`user_id`, `type`, `target_id`),
  INDEX idx_type_target (`type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户订阅关系';

CREATE TABLE `notification` (
  `id`          BIGINT       PRIMARY KEY AUTO_INCREMENT,
  `type`        VARCHAR(50)  NOT NULL COMMENT '通知类型',
  `title`       VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content`     VARCHAR(500) NOT NULL COMMENT '通知内容',
  `link`        VARCHAR(255) DEFAULT '' COMMENT '跳转链接',
  `actor_id`    BIGINT       COMMENT '触发者ID',
  `actor_name`  VARCHAR(100) DEFAULT '' COMMENT '触发者名称',
  `target_type` VARCHAR(50)  DEFAULT '' COMMENT '目标类型',
  `target_id`   BIGINT       COMMENT '目标ID',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_type_time (`type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知事件';

CREATE TABLE `user_notification` (
  `id`              BIGINT   PRIMARY KEY AUTO_INCREMENT,
  `user_id`         BIGINT   NOT NULL COMMENT '用户ID',
  `notification_id` BIGINT   NOT NULL COMMENT '通知事件ID',
  `is_read`         TINYINT  DEFAULT 0 COMMENT '是否已读',
  `read_at`         DATETIME COMMENT '已读时间',
  `is_deleted`      TINYINT  DEFAULT 0 COMMENT '是否删除',
  `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_unread (`user_id`, `is_read`, `is_deleted`, `create_time`),
  INDEX idx_notification (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知收件箱';


-- =============================================
-- 初始数据
-- =============================================

-- 默认管理员账号（密码已用 BCrypt 加密，请首次登录后修改）
-- admin / admin123（请首次登录后修改）
INSERT INTO `user` (`email`, `username`, `password`, `role`) VALUES
('admin@blog.com',      'admin',       '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN');

-- 6个分类
INSERT INTO `category` (`name`, `description`, `sort_order`) VALUES
('技术分享', '技术相关文章',           1),
('生活随笔', '生活感悟和随笔',         2),
('学习笔记', '学习过程中的笔记',       3),
('工具效率', '开发效率工具与技巧',     6);

-- 网站导航（30条：开发工具6 + 文档手册8 + 技术社区5 + 学习资源7 + AI工具4）
INSERT INTO `website_nav` (`name`, `url`, `description`, `category`, `sort_order`) VALUES
('GitHub',            'https://github.com',                                    '全球最大代码托管平台',                    '开发工具', 1),
('Stack Overflow',    'https://stackoverflow.com',                             '程序员问答社区',                          '开发工具', 2),
('Regex101',          'https://regex101.com',                                  '在线正则表达式调试工具，支持多语言',       '开发工具', 3),
('JSON Formatter',    'https://jsonformatter.curiousconcept.com',               'JSON 格式化、验证、对比工具',              '开发工具', 4),
('cron 表达式生成',    'https://crontab.guru',                                  '在线 cron 表达式生成与解析工具',           '开发工具', 5),
('carbon',            'https://carbon.now.sh',                                 '将代码生成精美截图，适合分享到社交媒体',   '开发工具', 6),
('MDN Web Docs',      'https://developer.mozilla.org',                         'Mozilla 官方 Web 开发文档',                '文档手册', 1),
('Spring 官网',       'https://spring.io',                                     'Spring 框架官方网站',                      '文档手册', 2),
('Vue 官网',          'https://vuejs.org',                                     'Vue.js 官方文档',                          '文档手册', 3),
('Redis 官方文档',    'https://redis.io/docs/',                                'Redis 官方文档，命令参考和最佳实践',       '文档手册', 4),
('Docker 文档',       'https://docs.docker.com',                               'Docker 官方文档，从入门到生产部署',        '文档手册', 5),
('Kubernetes 文档',   'https://kubernetes.io/zh-cn/docs',                      'K8s 中文官方文档，容器编排权威参考',       '文档手册', 6),
('MyBatis 文档',      'https://mybatis.org/mybatis-3/zh',                      'MyBatis 官方中文文档，SQL 映射框架',       '文档手册', 7),
('Linux man 手册',    'https://man7.org/linux/man-pages',                      'Linux 命令行手册在线版，权威参考',         '文档手册', 8),
('掘金',              'https://juejin.cn',                                     '面向开发者的技术社区',                     '技术社区', 1),
('InfoQ',             'https://www.infoq.cn',                                  '软件开发领域最具影响力的技术媒体',         '技术社区', 2),
('CSDN',              'https://www.csdn.net',                                  '中国最大的 IT 技术社区，海量技术博客',     '技术社区', 3),
('V2EX',              'https://v2ex.com',                                      '面向程序员的创意工作者社区',               '技术社区', 4),
('SegmentFault',      'https://segmentfault.com',                              '思否，专注于解决编程问题的技术问答社区',   '技术社区', 5),
('LeetCode',          'https://leetcode.cn',                                   '在线编程题库，刷算法必备',                 '学习资源', 1),
('Roadmap.sh',        'https://roadmap.sh',                                    '各方向技术学习路线图',                     '学习资源', 2),
('MIT OpenCourseWare','https://ocw.mit.edu',                                   'MIT 免费公开课，计算机科学顶级课程资源',   '学习资源', 3),
('Coursera',          'https://www.coursera.org',                              '全球顶级在线学习平台，提供名校认证课程',   '学习资源', 4),
('CS自学指南',         'https://csdiy.wiki',                                   '北大信科学长整理的计算机自学路径推荐',     '学习资源', 5),
('OI Wiki',           'https://oi-wiki.org',                                   '算法竞赛知识整合网站，数据结构与算法必备', '学习资源', 6),
('系统设计面试',       'https://github.com/donnemartin/system-design-primer',   '系统设计面试准备，GitHub 25万+ Star',      '学习资源', 7),
('ChatGPT',           'https://chat.openai.com',                               'OpenAI 官方 AI 对话助手',                  'AI 工具',  1),
('GitHub Copilot',    'https://github.com/features/copilot',                   'AI 代码补全工具，提升编码效率',            'AI 工具',  2),
('Hugging Face',      'https://huggingface.co',                                'AI 模型与数据集托管平台，开源 AI 社区',    'AI 工具',  3),
('Perplexity AI',     'https://www.perplexity.ai',                             '基于 AI 的搜索引擎，附带来源引用',         'AI 工具',  4);

-- =============================================
-- 精选文章（83条：架构设计35 + 底层原理9 + 数据库9 + 编程语言10 + 前端开发10 + 工程实践10）
-- =============================================

-- 架构设计（35条）
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
('The Twelve-Factor App',                       'https://12factor.net/zh_cn',                                                                                            '架构设计', '构建 SaaS 应用的方法论，12 条原则指导云原生开发',                                    1),
('Martin Fowler: Microservices',                'https://martinfowler.com/articles/microservices.html',                                                                  '架构设计', '微服务架构的定义性文章，Martin Fowler 原作，必读经典',                                 2),
('Clean Architecture',                          'https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html',                                          '架构设计', 'Uncle Bob 提出的整洁架构，依赖方向始终朝向内层',                                       3),
('CQRS 模式详解',                               'https://martinfowler.com/bliki/CQRS.html',                                                                              '架构设计', '命令查询责任分离模式，适用于读写负载差异大的系统',                                     4),
('事件驱动架构 (EDA) 指南',                      'https://martinfowler.com/articles/201701-event-driven.html',                                                            '架构设计', 'Martin Fowler 对事件驱动四种模式的深度分析',                                           5),
('API 设计最佳实践',                             'https://stackoverflow.blog/2020/03/02/best-practices-for-rest-api-design/',                                             '架构设计', 'Stack Overflow 出品的 REST API 设计规范与实战建议',                                    6),
('分布式系统的 CAP 理论',                        'https://www.infoq.cn/article/cap-twelve-years-later-how-the-rules-have-changed',                                        '架构设计', 'CAP 理论提出12年后的重新审视，深入理解 C、A、P 的权衡',                                7),
('Netflix 技术博客：混沌工程',                   'https://netflixtechblog.com/tagged/chaos-engineering',                                                                  '架构设计', 'Netflix 混沌工程实践，通过主动注入故障提升系统韧性',                                   8),
('Google SRE 手册',                             'https://sre.google/sre-book/table-of-contents/',                                                                        '架构设计', 'Google SRE 完整在线版，SLO/SLI/错误预算等概念的权威来源',                              9),
('高并发系统设计 40 问',                         'https://time.geekbang.org/column/intro/100035801',                                                                      '架构设计', '极客时间出品，系统讲解缓存、消息队列、分库分表等高并发方案',                            10),
('领域驱动设计（DDD）入门',                      'https://martinfowler.com/tags/domain%20driven%20design.html',                                                           '架构设计', 'Martin Fowler 关于 DDD 系列文章汇总，战略设计与战术设计',                             11),
('六边形架构（端口与适配器）',                    'https://alistair.cockburn.us/hexagonal-architecture/',                                                                  '架构设计', 'Alistair Cockburn 原文，将核心业务与外部系统彻底隔离的架构模式',                       12),
('Saga 分布式事务模式',                          'https://microservices.io/patterns/data/saga.html',                                                                      '架构设计', 'Chris Richardson 微服务模式网站，Saga 协调式与编排式详解',                             13),
('服务网格（Service Mesh）原理',                 'https://istio.io/latest/docs/concepts/what-is-istio/',                                                                  '架构设计', 'Istio 官方概念文档，理解 Service Mesh 的流量管理与可观测性',                           14),
('背压（Backpressure）机制详解',                 'https://www.reactivemanifesto.org/',                                                                                    '架构设计', '响应式宣言，异步系统中背压的重要性与实现方式',                                         15),
('数据库分片（Sharding）策略',                   'https://use-the-index-luke.com/sql/partitioning',                                                                       '架构设计', '水平分片、垂直分片的选择依据与常见分片键设计陷阱',                                     16),
('幂等性设计：接口重试的正确姿势',               'https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/',                                     '架构设计', 'AWS Builder Library，幂等 API 设计防止重复扣款、重复下单',                             17),
('Rate Limiting 限流算法对比',                   'https://konghq.com/blog/how-to-design-a-scalable-rate-limiting-algorithm',                                              '架构设计', '令牌桶、漏桶、滑动窗口四种限流算法原理与适用场景对比',                                 18),
('蓝绿部署与金丝雀发布',                         'https://martinfowler.com/bliki/BlueGreenDeployment.html',                                                               '架构设计', 'Martin Fowler 蓝绿部署与 Canary Release 原文，零停机发布方案',                        19),
('API Gateway 设计模式',                         'https://microservices.io/patterns/apigateway.html',                                                                     '架构设计', '微服务 API 网关模式，解决客户端多服务调用的聚合与路由问题',                            20),
('断路器（Circuit Breaker）模式',                'https://martinfowler.com/bliki/CircuitBreaker.html',                                                                    '架构设计', 'Martin Fowler 断路器原文，防止级联故障蔓延的核心容错模式',                             21),
('SOLID 原则实战解析',                           'https://blog.cleancoder.com/uncle-bob/2020/10/18/Solid-Relevance.html',                                                 '架构设计', 'Uncle Bob 亲解 SOLID 在现代软件开发中的意义与误区',                                    22),
('事件溯源（Event Sourcing）',                   'https://martinfowler.com/eaaDev/EventSourcing.html',                                                                    '架构设计', '以事件日志作为数据存储的核心模式，配合 CQRS 构建可审计系统',                          23),
('Strangler Fig：遗留系统改造',                  'https://martinfowler.com/bliki/StranglerFigApplication.html',                                                           '架构设计', '勒死无花果树模式，渐进式替换遗留系统而非一次性重写',                                   24),
('两阶段提交（2PC）与 3PC',                      'https://www.cs.princeton.edu/courses/archive/fall16/cos418/docs/L6-2pc.html',                                            '架构设计', 'Princeton 分布式课程讲义，2PC 协议原理与阻塞问题分析',                                 25),
('一致性哈希算法',                               'https://tom-e-white.com/2007/11/consistent-hashing-and-random-trees.html',                                              '架构设计', '一致性哈希原始论文，解决分布式缓存节点增减时的数据迁移问题',                           26),
('读写分离架构实践',                             'https://dev.mysql.com/doc/refman/8.0/en/replication.html',                                                              '架构设计', 'MySQL 主从复制官方文档，读写分离架构的数据一致性保障',                                 27),
('Outbox Pattern：事务性消息发送',               'https://microservices.io/patterns/data/transactional-outbox.html',                                                      '架构设计', '解决数据库写入与消息发送原子性问题的 Outbox 模式详解',                                 28),
('云原生应用设计原则',                           'https://www.cncf.io/blog/2019/08/22/how-do-cloud-native-and-kubernetes-serve-as-the-foundation-of-5g/',                 '架构设计', 'CNCF 云原生基金会，容器化、动态编排、微服务的设计理念',                                 29),
('eBay 亿级数据架构演进',                        'https://www.ebayinc.com/stories/blogs/tech/architecture-evolution/',                                                     '架构设计', 'eBay 技术博客，从单体到分布式的真实架构演进历程',                                       30),
('Twitter 时间线架构设计',                       'https://highscalability.com/blog/2013/7/8/the-architecture-twitter-uses-to-deal-with-150m-active-users.html',  '架构设计', 'High Scalability 解析 Twitter 1.5亿用户的 Feed 流分发架构',                             31),
('Pinterest 从零到百亿 PV',                      'https://highscalability.com/blog/2013/4/15/scaling-pinterest-from-0-to-10s-of-billions-of-page-views-a.html', '架构设计', 'Pinterest 从 0 到百亿 PV 的数据库拆分与缓存演进实录',                                   32),
('Uber 实时系统设计',                            'https://eng.uber.com/real-time-exactly-once-ad-event-processing/',                                                      '架构设计', 'Uber 工程博客，Exactly-Once 语义在实时广告事件处理中的实现',                           33),
('Discord 从 MongoDB 迁移至 Cassandra',          'https://discord.com/blog/how-discord-stores-billions-of-messages',                                                      '架构设计', 'Discord 如何存储数十亿条消息，NoSQL 选型与数据建模实践',                               34),
('Cloudflare 边缘计算架构',                      'https://blog.cloudflare.com/cloudflare-workers-the-architecture/',                                                      '架构设计', 'Cloudflare Workers 架构解析，边缘节点无服务器计算的实现原理',                          35);

-- 底层原理（9条）
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
('What Every Programmer Should Know About Memory', 'https://people.freebsd.org/~lstewart/articles/cpumemory.pdf',        '底层原理', 'Ulrich Drepper 经典论文，深入理解内存工作原理',                         1),
('深入 Linux 内核：进程调度',                      'https://www.kernel.org/doc/html/latest/scheduler/index.html',           '底层原理', 'Linux 官方文档，CFS 完全公平调度器的设计与实现',                       2),
('TCP/IP 协议栈图解',                              'https://www.ruanyifeng.com/blog/2012/06/internet_protocol_suite_part_i.html', '底层原理', '阮一峰图解 TCP/IP 协议族，通俗易懂，适合系统复习',               3),
('垃圾收集算法手册',                               'https://gchandbook.org/',                                               '底层原理', 'GC 领域最权威的学术参考，覆盖标记清除、引用计数、G1 等所有算法',    4),
('CPU 缓存与内存屏障',                             'https://www.aristeia.com/Papers/DDJ_Jul_Aug_2004_revised.pdf',           '底层原理', 'Scott Meyers 经典论文，解释多核 CPU 的缓存一致性与内存可见性',      5),
('浏览器工作原理详解',                             'https://www.html5rocks.com/zh/tutorials/internals/howbrowserswork/',     '底层原理', 'Tali Garsiel 深入剖析浏览器渲染引擎，前端开发者必读',                6),
('操作系统：从程序员视角',                         'https://csapp.cs.cmu.edu/',                                             '底层原理', 'CMU CSAPP 官网，深入理解计算机系统，硬核程序员进阶必读',              7),
('JVM 规范（Java SE 21）',                         'https://docs.oracle.com/javase/specs/jvms/se21/html/index.html',         '底层原理', 'Oracle 官方 JVM 规范，字节码、类加载、内存模型的最权威参考',         8),
('V8 引擎工作原理',                                'https://v8.dev/blog/elements-kinds',                                     '底层原理', 'V8 官方博客，讲解 JavaScript 对象的内部表示和优化策略',               9);

-- 数据库（9条）
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
('Designing Data-Intensive Applications 读书笔记', 'https://github.com/Vonng/ddia',                                                         '数据库', 'DDIA 中文翻译，分布式系统必读经典',                                                 1),
('MySQL 官方文档：索引优化',                       'https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html',                     '数据库', 'MySQL 8.0 官方索引优化指南，包含 EXPLAIN 详细解读',                                2),
('PostgreSQL 与 MySQL 深度对比',                   'https://www.enterprisedb.com/blog/postgresql-vs-mysql-360-degree-comparison',           '数据库', '全方位对比两大开源数据库的性能、功能、适用场景',                                    3),
('Redis 设计与实现',                               'http://redisbook.com/',                                                                 '数据库', '黄健宏著，深入解析 Redis 各数据结构和核心机制的中文资源',                          4),
('数据库事务隔离级别图解',                         'https://www.postgresql.org/docs/current/transaction-iso.html',                          '数据库', 'PostgreSQL 官方文档，图文并茂解释脏读、幻读、不可重复读',                          5),
('ShardingSphere 分库分表实战',                    'https://shardingsphere.apache.org/document/current/cn/user-manual/',                     '数据库', 'Apache ShardingSphere 官方中文文档，分库分表最佳实践',                             6),
('时序数据库选型指南',                             'https://db-engines.com/en/ranking/time+series+dbms',                                    '数据库', 'InfluxDB、TimescaleDB、Prometheus 等时序数据库对比与选型',                         7),
('Elasticsearch 权威指南（中文版）',               'https://www.elastic.co/guide/cn/elasticsearch/guide/current/index.html',                '数据库', 'Elastic 官方中文翻译版，全文搜索与分析入门到进阶',                                 8),
('SQL 性能调优：Execution Plans',                  'https://use-the-index-luke.com/',                                                       '数据库', '专注于 SQL 索引优化的免费在线书，配有大量执行计划图示',                            9);

-- 编程语言（10条）
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
('Effective Java 第3版（中文摘要）', 'https://github.com/clxering/Effective-Java-3rd-edition-Chinese-English-bilingual', '编程语言', 'Effective Java 中英文对照 GitHub 项目，90条最佳实践',                       1),
('Java 并发编程实战（JCIP）',        'https://jcip.net/',                                                                   '编程语言', 'Brian Goetz 著，Java 并发编程领域最权威的参考书',                           2),
('Oracle Java 官方教程',             'https://docs.oracle.com/javase/tutorial/',                                             '编程语言', 'Oracle 官方 Java 教程，覆盖从基础到高级的完整学习路径',                     3),
('Baeldung：Spring 系列教程',        'https://www.baeldung.com/spring-tutorial',                                             '编程语言', '最好的 Spring 英文教程网站，每篇都有代码示例，持续更新',                   4),
('Java 函数式编程指南',              'https://www.manning.com/books/modern-java-in-action',                                   '编程语言', 'Modern Java in Action，Lambda、Stream、Optional 深度实战',                  5),
('Project Reactor 响应式编程',       'https://projectreactor.io/docs/core/release/reference/',                               '编程语言', 'WebFlux 底层，Reactor 官方文档，响应式编程入门必读',                         6),
('JMH 基准测试入门',                 'https://openjdk.org/projects/code-tools/jmh/',                                         '编程语言', 'OpenJDK 官方基准测试框架，Java 性能测试的标准工具',                         7),
('Arthas 线上诊断工具',              'https://arthas.aliyun.com/doc/',                                                       '编程语言', '阿里巴巴开源的 Java 诊断工具，不重启实时排查生产问题',                      8),
('Java 新特性汇总（8~21）',          'https://openjdk.org/projects/jdk/',                                                    '编程语言', 'OpenJDK 各版本 JEP 特性列表，Java 8 到 21 新特性速查',                     9),
('JVM 调优实战案例',                 'https://heapsampling.blogspot.com/',                                                   '编程语言', '真实 JVM 调优案例分析，GC 日志解读与参数优化',                             10);

-- 前端开发（10条）
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
('MDN 前端学习路径',               'https://developer.mozilla.org/zh-CN/docs/Learn',              '前端开发', 'Mozilla 官方前端学习路径，HTML/CSS/JS 从零到实战',                      1),
('Vue 3 官方文档（中文）',         'https://cn.vuejs.org/guide/introduction.html',                '前端开发', 'Vue 3 官方中文文档，Composition API 最权威的参考',                      2),
('React 官方文档',                 'https://react.dev/learn',                                     '前端开发', 'React 新版官方文档，全面使用 Hooks 重写，示例丰富',                     3),
('TypeScript 深入理解',            'https://www.typescriptlang.org/docs/handbook/intro.html',     '前端开发', 'TypeScript 官方手册，类型系统、泛型、装饰器完整讲解',                   4),
('Web 性能优化指南',               'https://web.dev/performance/',                                '前端开发', 'Google web.dev 出品，Core Web Vitals 和性能优化最佳实践',               5),
('CSS 权威指南',                   'https://css-tricks.com/',                                     '前端开发', 'CSS-Tricks，最全面的 CSS 技巧网站，Flexbox/Grid 必备',                  6),
('Vite 官方文档',                  'https://cn.vitejs.dev/',                                      '前端开发', 'Vite 中文官方文档，新一代前端构建工具，冷启动极速',                     7),
('JavaScript 事件循环可视化',      'https://www.jsv9000.app/',                                    '前端开发', '交互式可视化 JS 事件循环、任务队列、微任务队列执行过程',                 8),
('WebAssembly 入门',               'https://webassembly.org/getting-started/developers-guide/',   '前端开发', 'WebAssembly 官方入门指南，在浏览器中运行 C/Rust 代码',                  9),
('前端工程化：Module Federation',  'https://module-federation.io/',                               '前端开发', 'Webpack 5 模块联邦，微前端架构实现方案官方文档',                        10);

-- 工程实践（10条）
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
('Google 工程实践文档',         'https://google.github.io/eng-practices/',                                                                    '工程实践', 'Google 官方 Code Review 最佳实践，评审者和作者双视角',                1),
('The Pragmatic Programmer',    'https://pragprog.com/titles/tpp20/the-pragmatic-programmer-20th-anniversary-edition/',                       '工程实践', '程序员修炼之道20周年版，每个职业程序员必读的经典',                    2),
('持续集成持续交付（CI/CD）',   'https://www.martinfowler.com/articles/continuousIntegration.html',                                           '工程实践', 'Martin Fowler 关于 CI 的原始论文，理解 CI/CD 的思想基础',            3),
('代码整洁之道（Clean Code）',  'https://www.oreilly.com/library/view/clean-code-a/9780136083238/',                                           '工程实践', 'Robert C. Martin 著，如何写出可读、可维护的高质量代码',               4),
('GitHub Actions 工作流',       'https://docs.github.com/zh/actions',                                                                        '工程实践', 'GitHub Actions 官方中文文档，自动化 CI/CD 流水线配置',               5),
('测试驱动开发（TDD）实践',     'https://martinfowler.com/bliki/TestDrivenDevelopment.html',                                                  '工程实践', 'Martin Fowler 对 TDD 的定义与实践建议，改变编程习惯的方法',          6),
('监控告警体系建设',            'https://grafana.com/docs/grafana/latest/',                                                                   '工程实践', 'Grafana 官方文档，从零搭建 Prometheus + Grafana 监控体系',            7),
('OpenAPI 3.0 规范',            'https://swagger.io/specification/',                                                                          '工程实践', 'Swagger/OpenAPI 3.0 规范，API 文档标准化的行业标准',                  8),
('技术债务管理实践',            'https://martinfowler.com/bliki/TechnicalDebt.html',                                                         '工程实践', 'Martin Fowler 技术债务四象限模型，理性看待和处理技术债',             9),
('敏捷开发宣言与原则',          'https://agilemanifesto.org/iso/zhchs/manifesto.html',                                                       '工程实践', '敏捷宣言中文版，12条原则指导软件开发团队协作与交付',                10);

use blog;
