-- 个人博客系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE blog_system;

-- 1. 用户表
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) COMMENT '密码（OAuth用户可为空）',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1正常 0禁用',
  `gitee_id` VARCHAR(50) UNIQUE COMMENT 'Gitee用户ID',
  `oauth_provider` VARCHAR(20) COMMENT 'OAuth提供商: gitee/github',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (`email`),
  INDEX idx_username (`username`),
  INDEX idx_gitee_id (`gitee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 分类表
CREATE TABLE `category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
  `description` VARCHAR(200) COMMENT '分类描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 3. 文章表
CREATE TABLE `article` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` LONGTEXT NOT NULL COMMENT 'Markdown内容',
  `summary` VARCHAR(500) COMMENT '摘要',
  `cover_image` VARCHAR(255) COMMENT '封面图',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `author_id` BIGINT NOT NULL COMMENT '作者ID',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1发布 0草稿',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (`category_id`),
  INDEX idx_author (`author_id`),
  INDEX idx_create_time (`create_time`),
  INDEX idx_status (`status`),
  FULLTEXT idx_title_content (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 4. 评论表
CREATE TABLE `comment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_article (`article_id`),
  INDEX idx_parent (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 5. 文章点赞表
CREATE TABLE `article_like` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `article_id` BIGINT NOT NULL COMMENT '文章ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_article_user (`article_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞表';

-- 6. 网站导航表
CREATE TABLE `website_nav` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL COMMENT '网站名称',
  `url` VARCHAR(500) NOT NULL COMMENT '网站URL',
  `description` VARCHAR(500) COMMENT '网站介绍',
  `category` VARCHAR(50) NOT NULL COMMENT '分类',
  `logo` VARCHAR(255) COMMENT '网站Logo',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站导航表';

-- 7. 论坛帖子表
CREATE TABLE `forum_post` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `user_id` BIGINT NOT NULL COMMENT '发帖人ID',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `reply_count` INT DEFAULT 0 COMMENT '回复数',
  `last_reply_time` DATETIME COMMENT '最后回复时间',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (`user_id`),
  INDEX idx_last_reply (`last_reply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛帖子表';

-- 8. 论坛回复表
CREATE TABLE `forum_reply` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '回复人ID',
  `content` TEXT NOT NULL COMMENT '回复内容',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父回复ID',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_post (`post_id`),
  INDEX idx_user (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='论坛回复表';

-- 9. 精选文章表
CREATE TABLE `featured_article` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
  `url` VARCHAR(500) NOT NULL COMMENT '文章URL',
  `category` VARCHAR(50) NOT NULL COMMENT '类别',
  `description` VARCHAR(500) COMMENT '描述',
  `cover_image` VARCHAR(255) COMMENT '封面图',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='精选文章表';

-- 10. 访问统计表
CREATE TABLE `visit_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT COMMENT '用户ID(未登录为NULL)',
  `ip` VARCHAR(50) NOT NULL COMMENT 'IP地址',
  `url` VARCHAR(500) NOT NULL COMMENT '访问URL',
  `user_agent` VARCHAR(500) COMMENT 'User Agent',
  `referer` VARCHAR(500) COMMENT '来源',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_create_time (`create_time`),
  INDEX idx_url (`url`(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问日志表';

-- 11. 每日统计表（每天一行，由定时任务写入，替代明细日志表）
CREATE TABLE `daily_stats` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `stat_date` DATE NOT NULL UNIQUE COMMENT '统计日期',
  `pv` BIGINT DEFAULT 0 COMMENT '页面访问量',
  `uv` BIGINT DEFAULT 0 COMMENT '独立访客数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_stat_date (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日访问统计表';

-- 12. 面经表
CREATE TABLE `interview` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` LONGTEXT NOT NULL COMMENT 'Markdown内容',
  `summary` VARCHAR(500) COMMENT '摘要',
  `cover_image` VARCHAR(255) COMMENT '封面图',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `author_id` BIGINT NOT NULL COMMENT '作者ID',
  `view_count` INT DEFAULT 0 COMMENT '浏览量',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `comment_count` INT DEFAULT 0 COMMENT '评论数',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1发布 0草稿',
  `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_category (`category_id`),
  INDEX idx_status (`status`),
  INDEX idx_create_time (`create_time`),
  FULLTEXT idx_title_content (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面经表';

-- 12. 面经评论表
CREATE TABLE `interview_comment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `interview_id` BIGINT NOT NULL COMMENT '面经ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `email` VARCHAR(100) COMMENT '邮箱',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT DEFAULT 1 COMMENT '状态: 1正常 0删除',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_interview (`interview_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面经评论表';

-- 插入初始数据
-- 默认管理员账号 (密码: admin123, 需要BCrypt加密)
INSERT INTO `user` (`email`, `username`, `password`, `role`) VALUES
('admin@blog.com', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN');

-- 默认分类
INSERT INTO `category` (`name`, `description`, `sort_order`) VALUES
('技术分享', '技术相关文章', 1),
('生活随笔', '生活感悟和随笔', 2),
('学习笔记', '学习过程中的笔记', 3);

-- =============================================
-- 博客系统测试数据
-- =============================================

USE blog_system;

-- 文章数据（author_id=1 为默认管理员）
INSERT INTO `article` (`title`, `content`, `summary`, `category_id`, `author_id`, `view_count`, `like_count`, `comment_count`, `status`, `is_top`) VALUES
                                                                                                                                                       (
                                                                                                                                                           'Spring Boot 3.x 最佳实践总结',
                                                                                                                                                           '## 前言\n\nSpring Boot 3.x 基于 Spring Framework 6，要求 JDK 17+，带来了许多重要变化。\n\n## 一、依赖注入\n\n推荐使用构造器注入代替字段注入：\n\n```java\n@Service\npublic class ArticleService {\n    private final ArticleRepository articleRepository;\n\n    public ArticleService(ArticleRepository articleRepository) {\n        this.articleRepository = articleRepository;\n    }\n}\n```\n\n## 二、配置文件\n\nSpring Boot 3.x 推荐使用 `application.yml`，结构清晰，支持多环境：\n\n```yaml\nspring:\n  datasource:\n    url: jdbc:mysql://localhost:3306/blog\n    username: root\n    password: change_me\n```\n\n## 三、AOT 与 GraalVM\n\n支持提前编译为原生镜像，大幅提升启动速度。\n\n## 总结\n\n拥抱新特性，写出更现代的 Java 代码。',
                                                                                                                                                           'Spring Boot 3.x 升级了哪些内容？本文总结构造器注入、yml 配置、AOT 编译等最佳实践。',
                                                                                                                                                           1, 1, 358, 42, 3, 1, 1
                                                                                                                                                       ),
                                                                                                                                                       (
                                                                                                                                                           'Vue 3 Composition API 实战指南',
                                                                                                                                                           '## 为什么用 Composition API\n\nOptions API 在组件逻辑复杂时难以复用，Composition API 通过函数组合解决了这个问题。\n\n## 基础用法\n\n```javascript\nimport { ref, computed, onMounted } from ''vue''\n\nexport default {\n  setup() {\n    const count = ref(0)\n    const double = computed(() => count.value * 2)\n\n    onMounted(() => {\n      console.log(''组件已挂载'')\n    })\n\n    return { count, double }\n  }\n}\n```\n\n## 使用 `<script setup>`\n\n```vue\n<script setup>\nimport { ref } from ''vue''\nconst msg = ref(''Hello'')\n</script>\n\n<template>\n  <p>{{ msg }}</p>\n</template>\n```\n\n## 自定义 Hook\n\n```javascript\n// useCounter.js\nexport function useCounter() {\n  const count = ref(0)\n  const increment = () => count.value++\n  return { count, increment }\n}\n```\n\n组合式 API 让逻辑真正可复用。',
                                                                                                                                                           '从 Options API 到 Composition API，一文搞懂 Vue 3 的正确打开方式，含自定义 Hook 实战。',
                                                                                                                                                           1, 1, 215, 28, 2, 1, 0
                                                                                                                                                       ),
                                                                                                                                                       (
                                                                                                                                                           'Redis 缓存设计：从入门到实践',
                                                                                                                                                           '## 为什么需要缓存\n\n数据库查询耗时，热点数据反复读取浪费资源。Redis 作为内存数据库，读写速度是 MySQL 的百倍以上。\n\n## 常见缓存模式\n\n### Cache Aside（旁路缓存）\n\n读：先查缓存，命中直接返回；未命中查数据库，回写缓存。\n写：先更新数据库，再删除缓存。\n\n```java\npublic Category getById(Long id) {\n    String key = "category:" + id;\n    Category cache = (Category) redisTemplate.opsForValue().get(key);\n    if (cache != null) return cache;\n    Category db = categoryMapper.selectById(id);\n    redisTemplate.opsForValue().set(key, db, 1, TimeUnit.HOURS);\n    return db;\n}\n```\n\n## 缓存穿透\n\n查询不存在的数据，缓存不生效，每次打到数据库。\n\n**解决**：布隆过滤器 + 缓存空值。\n\n## 缓存雪崩\n\n大量缓存同时过期，数据库瞬间压力飙升。\n\n**解决**：过期时间加随机值，避免集中失效。\n\n## 总结\n\n合理使用缓存能显著提升系统吞吐量。',
                                                                                                                                                           '详解 Redis 缓存旁路模式，以及缓存穿透、缓存雪崩的原因与解决方案，附 Java 代码示例。',
                                                                                                                                                           1, 1, 189, 35, 1, 1, 0
                                                                                                                                                       ),
                                                                                                                                                       (
                                                                                                                                                           '2024 年终总结：代码之外的成长',
                                                                                                                                                           '## 回顾这一年\n\n今年写了大概 **12 万行代码**，提交了 800+ 次 commit，上线了 3 个项目。\n\n但让我收获最多的，不是这些数字。\n\n## 学会了说"不"\n\n需求评审时，我开始敢于指出不合理的设计，而不是一味接受。技术债务是实实在在的成本。\n\n## 输出倒逼输入\n\n写博客让我意识到很多东西"以为懂了，其实没懂"。能把复杂概念讲清楚，才算真正理解。\n\n## 健康是 1，其他是 0\n\n三月份腰椎出了问题，才意识到久坐的代价。现在坚持每天散步 30 分钟。\n\n## 2025 年计划\n\n- 深入学习分布式系统\n- 坚持写博客，至少 24 篇\n- 每周运动 3 次\n\n愿新的一年，代码少 bug，生活多惊喜。',
                                                                                                                                                           '写了 12 万行代码的一年，我学到的最重要的东西不是技术，而是如何更好地成为自己。',
                                                                                                                                                           2, 1, 512, 67, 4, 1, 0
                                                                                                                                                       ),
                                                                                                                                                       (
                                                                                                                                                           'MySQL 索引优化实战：慢查询调优全过程',
                                                                                                                                                           '## 发现慢查询\n\n开启慢查询日志：\n\n```sql\nSET GLOBAL slow_query_log = ON;\nSET GLOBAL long_query_time = 1;\n```\n\n## 分析执行计划\n\n```sql\nEXPLAIN SELECT * FROM article\nWHERE category_id = 1 AND status = 1\nORDER BY create_time DESC\nLIMIT 10;\n```\n\n关注 `type`、`key`、`rows` 三列。\n\n## 常见索引失效场景\n\n1. 对索引列做函数运算：`WHERE YEAR(create_time) = 2024` ❌\n2. 隐式类型转换：`WHERE id = ''123''` ❌\n3. `LIKE` 前缀通配符：`WHERE title LIKE ''%关键词''` ❌\n4. `OR` 连接非索引列 ❌\n\n## 联合索引最左前缀\n\n索引 `(category_id, status, create_time)`：\n- `WHERE category_id = 1` ✅\n- `WHERE category_id = 1 AND status = 1` ✅\n- `WHERE status = 1` ❌（跳过最左列）\n\n## 总结\n\nEXPLAIN 是最好的老师，先看执行计划再建索引。',
                                                                                                                                                           'EXPLAIN 分析执行计划、索引失效场景、联合索引最左前缀——一次完整的 MySQL 慢查询调优记录。',
                                                                                                                                                           3, 1, 423, 51, 2, 1, 0
                                                                                                                                                       );

-- 评论数据
INSERT INTO `comment` (`article_id`, `parent_id`, `content`, `nickname`, `email`) VALUES
-- 文章1的评论
(1, 0, '写得很好！构造器注入的方式我一直在用，确实比字段注入更清晰，单元测试也方便很多。', '张三', 'zhangsan@example.com'),
(1, 0, '请问 AOT 编译在生产环境稳定吗？公司项目想升级 Spring Boot 3，有点担心兼容性问题。', '李四', NULL),
(1, 2, '我们已经在生产用了半年，稳定性没问题，主要注意第三方库的兼容性，升级前建议先检查依赖版本。', '博主', 'admin@blog.com'),
-- 文章2的评论
(2, 0, '终于看到一篇讲清楚 Composition API 的文章！自定义 Hook 那个例子非常实用。', '王五', 'wangwu@qq.com'),
(2, 0, '`<script setup>` 是真的香，代码量少了很多，强烈推荐！', '前端小白', NULL),
-- 文章3的评论
(3, 0, '缓存雪崩的随机过期时间方案很实用，之前踩过这个坑，血泪教训。', '赵六', 'zhao@163.com'),
-- 文章4的评论
(4, 0, '年终总结写得很真实，"输出倒逼输入"这句话戳到我了，转发收藏！', '匿名读者', NULL),
(4, 0, '腰椎问题真的要注意，程序员职业病。买个好椅子和站立办公桌是值得的投资。', '老程序员', 'olddev@gmail.com'),
(4, 0, '感谢分享，祝 2025 年一切顺利！', '路过的朋友', NULL),
(4, 8, '谢谢，同祝！', '博主', 'admin@blog.com'),
-- 文章5的评论
(5, 0, 'EXPLAIN 这个工具太重要了，上周刚用它优化了一个查询，从 8 秒降到 0.02 秒！', '数据库新手', NULL),
(5, 0, '最左前缀原则那里讲得特别清楚，终于理解为什么有些索引不生效了。', '后端开发', 'backend@example.com');

-- 更新评论计数（与上方评论数据对应）
UPDATE `article` SET `comment_count` = 3 WHERE `id` = 1;
UPDATE `article` SET `comment_count` = 2 WHERE `id` = 2;
UPDATE `article` SET `comment_count` = 1 WHERE `id` = 3;
UPDATE `article` SET `comment_count` = 4 WHERE `id` = 4;
UPDATE `article` SET `comment_count` = 2 WHERE `id` = 5;

-- 网站导航数据
INSERT INTO `website_nav` (`name`, `url`, `description`, `category`, `sort_order`) VALUES
                                                                                       ('GitHub',       'https://github.com',           '全球最大代码托管平台',         '开发工具', 1),
                                                                                       ('Stack Overflow','https://stackoverflow.com',   '程序员问答社区',               '开发工具', 2),
                                                                                       ('MDN Web Docs', 'https://developer.mozilla.org','Mozilla 官方 Web 开发文档',    '文档手册', 1),
                                                                                       ('Spring 官网',  'https://spring.io',            'Spring 框架官方网站',          '文档手册', 2),
                                                                                       ('Vue 官网',     'https://vuejs.org',            'Vue.js 官方文档',              '文档手册', 3),
                                                                                       ('掘金',         'https://juejin.cn',            '面向开发者的技术社区',          '技术社区', 1),
                                                                                       ('LeetCode',     'https://leetcode.cn',          '在线编程题库，刷算法必备',      '学习资源', 1),
                                                                                       ('Roadmap.sh',   'https://roadmap.sh',           '各方向技术学习路线图',          '学习资源', 2);

-- 精选文章数据
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
                                                                                             ('The Twelve-Factor App',           'https://12factor.net/zh_cn',     '架构设计', '构建 SaaS 应用的方法论，12 条原则指导云原生开发',  1),
                                                                                             ('What Every Programmer Should Know About Memory', 'https://people.freebsd.org/~lstewart/articles/cpumemory.pdf', '底层原理', 'Ulrich Drepper 经典论文，深入理解内存工作原理', 2),
                                                                                             ('Designing Data-Intensive Applications 读书笔记', 'https://github.com/Vonng/ddia',  '数据库',   'DDIA 中文翻译，分布式系统必读经典',              3);

USE blog_system;

-- =============================================
-- 1. 新增分类
-- =============================================
INSERT INTO `category` (`name`, `description`, `sort_order`) VALUES
                                                                 ('开源项目', '值得关注的开源项目介绍', 4),
                                                                 ('面试总结', '技术面试经验与题目整理', 5),
                                                                 ('工具效率', '开发效率工具与技巧', 6);


-- =============================================
-- 2. 新增文章（共15篇，总量达20篇）
-- =============================================
INSERT INTO `article` (`title`, `content`, `summary`, `category_id`, `author_id`, `view_count`, `like_count`, `comment_count`, `status`, `is_top`) VALUES

-- 技术分享 (category_id=1)
('深入理解 JVM 内存模型',
 '## JVM 内存结构\n\nJVM 内存分为以下几个区域：\n\n- **堆（Heap）**：存放对象实例，是 GC 的主要区域\n- **方法区（Method Area）**：存放类信息、常量、静态变量\n- **虚拟机栈（VM Stack）**：每个线程一个，存放栈帧\n- **本地方法栈**：为 Native 方法服务\n- **程序计数器**：当前线程执行的字节码行号\n\n## 堆内存分代\n\n```\nHeap\n├── Young Generation\n│   ├── Eden Space\n│   ├── Survivor S0\n│   └── Survivor S1\n└── Old Generation\n```\n\n## GC 触发条件\n\n- Minor GC：Eden 区满时触发\n- Major GC：老年代空间不足\n- Full GC：整个堆空间不足，会 STW\n\n## 常用 JVM 参数\n\n```bash\n-Xms512m -Xmx2g        # 初始/最大堆\n-XX:+UseG1GC           # 使用 G1 收集器\n-XX:MaxGCPauseMillis=200 # GC 最大停顿时间\n```',
 '深入讲解 JVM 堆、栈、方法区的内存布局，以及 Minor GC、Major GC 的触发时机和常用调优参数。',
 1, 1, 623, 78, 2, 1, 0),

('Docker 容器化部署实战',
 '## 为什么用 Docker\n\n传统部署方式："在我机器上能跑" 的问题。Docker 通过容器技术实现环境隔离和一致性。\n\n## Dockerfile 编写\n\n```dockerfile\nFROM openjdk:17-jre-slim\nWORKDIR /app\nCOPY target/*.jar app.jar\nEXPOSE 8080\nENTRYPOINT ["java", "-jar", "app.jar"]\n```\n\n## docker-compose 编排\n\n```yaml\nversion: "3.8"\nservices:\n  app:\n    build: .\n    ports:\n      - "8080:8080"\n    depends_on:\n      - mysql\n  mysql:\n    image: mysql:8.0\n    environment:\n      MYSQL_ROOT_PASSWORD: change_me\n      MYSQL_DATABASE: blog_system\n```\n\n## 常用命令\n\n```bash\ndocker build -t blog:1.0 .\ndocker-compose up -d\ndocker logs -f blog_app\n```',
 '从 Dockerfile 编写到 docker-compose 多服务编排，完整演示 Spring Boot 应用容器化部署流程。',
 1, 1, 445, 56, 1, 1, 0),

('Kubernetes 入门：从 Pod 到 Service',
 '## 核心概念\n\n- **Pod**：最小调度单元，包含一个或多个容器\n- **Deployment**：管理 Pod 的期望状态\n- **Service**：为 Pod 提供稳定的访问入口\n- **Ingress**：管理外部访问路由\n\n## 部署示例\n\n```yaml\napiVersion: apps/v1\nkind: Deployment\nmetadata:\n  name: blog-app\nspec:\n  replicas: 3\n  selector:\n    matchLabels:\n      app: blog\n  template:\n    spec:\n      containers:\n      - name: blog\n        image: blog:1.0\n        ports:\n        - containerPort: 8080\n```\n\n## 常用命令\n\n```bash\nkubectl get pods\nkubectl describe pod blog-xxx\nkubectl logs blog-xxx\nkubectl scale deployment blog-app --replicas=5\n```',
 'K8s 入门：Pod、Deployment、Service 核心概念详解，附完整 YAML 配置示例和常用 kubectl 命令。',
 1, 1, 312, 41, 0, 1, 0),

('消息队列选型：Kafka vs RocketMQ vs RabbitMQ',
 '## 为什么需要消息队列\n\n- **异步解耦**：订单创建后异步发短信、扣库存\n- **流量削峰**：秒杀场景请求缓冲\n- **数据同步**：跨系统数据同步\n\n## 对比\n\n| 特性 | Kafka | RocketMQ | RabbitMQ |\n|------|-------|----------|----------|\n| 吞吐量 | 百万级/s | 十万级/s | 万级/s |\n| 延迟 | 毫秒级 | 毫秒级 | 微秒级 |\n| 消息顺序 | 分区内有序 | 支持 | 不保证 |\n| 事务消息 | 支持 | 支持 | 不支持 |\n| 适用场景 | 日志、流计算 | 电商业务 | 传统业务 |\n\n## 选型建议\n\n- 日志收集、大数据 → **Kafka**\n- 电商/金融业务 → **RocketMQ**\n- 复杂路由/老项目 → **RabbitMQ**',
 '详细对比 Kafka、RocketMQ、RabbitMQ 的吞吐量、延迟、事务支持等核心特性，给出选型建议。',
 1, 1, 289, 33, 0, 1, 0),

('分布式锁：从 Redis 到 Redisson',
 '## 为什么需要分布式锁\n\n单机 `synchronized` 在多实例部署下失效，需要跨进程的锁机制。\n\n## Redis SETNX 方案\n\n```java\n// 加锁\nBoolean locked = redisTemplate.opsForValue()\n    .setIfAbsent("lock:order:" + orderId, "1", 30, TimeUnit.SECONDS);\n\n// 释放锁（必须用 Lua 脚本保证原子性）\nString script = "if redis.call(''get'',KEYS[1]) == ARGV[1] then " +\n    "return redis.call(''del'',KEYS[1]) else return 0 end";\n```\n\n## Redisson 方案（推荐）\n\n```java\nRLock lock = redissonClient.getLock("lock:order:" + orderId);\ntry {\n    if (lock.tryLock(5, 30, TimeUnit.SECONDS)) {\n        // 业务逻辑\n    }\n} finally {\n    lock.unlock();\n}\n```\n\nRedisson 自动处理看门狗续期，避免业务未完成锁已过期的问题。',
 '从 Redis SETNX 手动实现到 Redisson 框架使用，彻底搞懂分布式锁的实现原理和常见坑。',
 1, 1, 534, 69, 3, 1, 0),

('HTTP/2 与 HTTP/3 核心特性对比',
 '## HTTP/1.1 的问题\n\n- 队头阻塞：同一连接串行处理请求\n- 无头部压缩：每次传输完整 Header\n- 不支持服务器推送\n\n## HTTP/2 改进\n\n- **多路复用**：一个连接并发多个请求\n- **头部压缩**：HPACK 算法压缩 Header\n- **服务器推送**：主动推送关联资源\n- **二进制分帧**：更高效的传输\n\n## HTTP/3 (QUIC)\n\n- 基于 **UDP** 而非 TCP\n- 彻底解决 TCP 层面的队头阻塞\n- 0-RTT 连接恢复\n- 内置 TLS 1.3\n\n## 性能数据\n\n弱网环境下，HTTP/3 页面加载速度比 HTTP/2 快 **20-30%**。',
 '深入对比 HTTP/1.1、HTTP/2、HTTP/3 的核心差异：多路复用、头部压缩、QUIC 协议等关键特性。',
 1, 1, 198, 24, 0, 1, 0),

-- 学习笔记 (category_id=3)
('Java 并发编程：ThreadPoolExecutor 深度解析',
 '## 为什么不用 Executors\n\n阿里规范禁止使用 `Executors.newFixedThreadPool()`，因为其队列无界，可能 OOM。\n\n## 核心参数\n\n```java\nnew ThreadPoolExecutor(\n    5,              // corePoolSize：核心线程数\n    20,             // maximumPoolSize：最大线程数\n    60L,            // keepAliveTime：空闲线程存活时间\n    TimeUnit.SECONDS,\n    new LinkedBlockingQueue<>(1000), // 有界队列\n    new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略\n);\n```\n\n## 执行流程\n\n1. 核心线程未满 → 创建核心线程\n2. 核心线程已满 → 放入队列\n3. 队列已满 → 创建非核心线程\n4. 达到最大线程数 → 执行拒绝策略\n\n## 四种拒绝策略\n\n- `AbortPolicy`：抛出异常（默认）\n- `CallerRunsPolicy`：调用者线程执行\n- `DiscardPolicy`：静默丢弃\n- `DiscardOldestPolicy`：丢弃最旧任务',
 '彻底搞懂 ThreadPoolExecutor 七个核心参数的含义、线程池执行流程和四种拒绝策略的选择。',
 3, 1, 445, 58, 1, 1, 0),

('设计模式实战：策略模式消除 if-else',
 '## 场景：支付方式选择\n\n```java\n// 糟糕的写法\nif ("alipay".equals(type)) {\n    // 支付宝逻辑\n} else if ("wechat".equals(type)) {\n    // 微信支付逻辑\n} else if ("bank".equals(type)) {\n    // 银行卡逻辑\n}\n```\n\n## 策略模式重构\n\n```java\npublic interface PayStrategy {\n    void pay(BigDecimal amount);\n    String type();\n}\n\n@Component\npublic class AlipayStrategy implements PayStrategy {\n    public void pay(BigDecimal amount) { /* ... */ }\n    public String type() { return "alipay"; }\n}\n\n// 注册中心\n@Component\npublic class PayContext {\n    private final Map<String, PayStrategy> strategies;\n\n    public PayContext(List<PayStrategy> list) {\n        strategies = list.stream()\n            .collect(Collectors.toMap(PayStrategy::type, s -> s));\n    }\n\n    public void pay(String type, BigDecimal amount) {\n        strategies.get(type).pay(amount);\n    }\n}\n```',
 '用策略模式 + Spring 自动注入彻底消除业务代码中的大量 if-else，代码可扩展性大幅提升。',
 3, 1, 367, 47, 0, 1, 0),

('Git 工作流：团队协作最佳实践',
 '## 常用工作流\n\n### Git Flow\n\n```\nmain ──────────────────────────────▶\n         ↑ merge\nrelease ─┤\n         ↑ merge\ndevelop ─┼──────────────────────────▶\n         ↑ branch\nfeature ─┘\n```\n\n### Trunk Based Development（推荐）\n\n所有人直接向 main 提交，依赖 feature flags 控制功能开关，适合 CI/CD 成熟的团队。\n\n## Commit 规范\n\n```\nfeat: 新功能\nfix: 修复 Bug\ndocs: 文档更新\nrefactor: 重构\ntest: 测试相关\nchore: 构建/工具链\n```\n\n## 实用命令\n\n```bash\n# 交互式变基，整理提交历史\ngit rebase -i HEAD~3\n\n# 找出引入 Bug 的提交\ngit bisect start\ngit bisect bad HEAD\ngit bisect good v1.0\n```',
 '介绍 Git Flow 和 Trunk Based Development 两种团队工作流的适用场景，以及 commit 规范和高效 git 命令。',
 3, 1, 256, 31, 0, 1, 0),

('Nginx 配置实战：反向代理与负载均衡',
 '## 反向代理配置\n\n```nginx\nserver {\n    listen 80;\n    server_name blog.example.com;\n\n    location / {\n        proxy_pass http://localhost:5173;\n        proxy_set_header Host $host;\n        proxy_set_header X-Real-IP $remote_addr;\n    }\n\n    location /api/ {\n        proxy_pass http://localhost:8080;\n    }\n}\n```\n\n## 负载均衡\n\n```nginx\nupstream backend {\n    least_conn;  # 最少连接策略\n    server 192.168.1.10:8080 weight=3;\n    server 192.168.1.11:8080 weight=1;\n    server 192.168.1.12:8080 backup;  # 备用\n}\n```\n\n## HTTPS 配置\n\n```nginx\nlisten 443 ssl;\nssl_certificate /etc/nginx/ssl/cert.pem;\nssl_certificate_key /etc/nginx/ssl/key.pem;\nssl_protocols TLSv1.2 TLSv1.3;\n```',
 'Nginx 反向代理、负载均衡（轮询/最少连接/权重）和 HTTPS 配置，前后端分离项目必会技能。',
 3, 1, 334, 42, 1, 1, 0),

-- 生活随笔 (category_id=2)
('程序员的读书方法：如何高效消化技术书籍',
 '## 我的读书层次\n\n**第一遍：快速过**\n扫目录，读引言，建立整体印象。30分钟内完成。\n\n**第二遍：精读核心章节**\n重点读与当前工作最相关的章节，做好笔记。\n\n**第三遍：实践验证**\n把书中的代码跑起来，改一改，看看会发生什么。\n\n## 费曼学习法\n\n学完一个概念，尝试用大白话解释给一个不懂技术的人听。解释不清楚的地方，就是真正没理解的地方。\n\n## 我读过最好的书\n\n- 《DDIA》——分布式系统圣经\n- 《深入理解 Java 虚拟机》——JVM 最权威\n- 《重构》——代码整洁的方法论\n- 《凤凰项目》——DevOps 思想小说',
 '分享我读技术书的三遍方法和费曼学习法，以及亲测有效的经典技术书单推荐。',
 2, 1, 387, 52, 2, 1, 0),

('远程工作两年：自律与效率的真相',
 '## 期待与现实\n\n入职远程岗位前，我以为会是咖啡馆、沙滩、自由时间。\n\n现实是：家里的床会让你永远不想起来。\n\n## 我找到的节奏\n\n**固定时间上下班**\n即使在家，也设定 9:00 开始，18:00 结束。超出这个范围坚决不看消息。\n\n**独立工作空间**\n哪怕是一张小桌子，只要坐上去就进入工作模式。离开就完全断开。\n\n**周报替代日报**\n记录本周做了什么、学到什么、明周计划，既梳理自己又同步团队。\n\n## 意外收获\n\n通勤时间归零，每天多出 2 小时学习。\n身体更健康，可以自己做午饭。\n家人关系更好，但需要设立边界。',
 '两年远程工作的真实体验：打破对自由工作的幻想，分享真正帮助我保持高效的具体方法。',
 2, 1, 521, 74, 3, 1, 0),

-- 面试总结 (category_id=5)
('Java 高频面试题：集合框架全解',
 '## HashMap 底层原理\n\n**JDK 1.8**：数组 + 链表 + 红黑树\n\n- 初始容量 16，负载因子 0.75\n- 链表长度 ≥ 8 且数组长度 ≥ 64 时转为红黑树\n- 扩容时容量翻倍，重新 rehash\n\n```java\n// 计算 hash（高16位与低16位异或，减少碰撞）\nstatic final int hash(Object key) {\n    int h;\n    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);\n}\n```\n\n## ConcurrentHashMap\n\nJDK 1.8 放弃 Segment 分段锁，改用 **CAS + synchronized**：\n- 空桶：CAS 插入\n- 有碰撞：synchronized 锁住链表头节点\n\n## LinkedHashMap\n\n继承 HashMap，额外维护双向链表记录插入顺序，可用于实现 **LRU 缓存**。',
 'HashMap 底层数组+链表+红黑树结构，扩容机制，ConcurrentHashMap 的 CAS+synchronized 方案，面试必考。',
 5, 1, 678, 89, 4, 1, 0),

('Spring 面试题精华：IOC、AOP、事务',
 '## IOC 容器初始化流程\n\n1. 加载配置（XML/注解/Java Config）\n2. 解析 BeanDefinition\n3. 实例化 Bean（反射）\n4. 依赖注入\n5. 初始化（@PostConstruct / InitializingBean）\n\n## AOP 动态代理\n\n- **JDK 动态代理**：目标类实现接口，基于接口生成代理\n- **CGLIB**：目标类无接口，生成子类代理\n\n## 事务传播行为\n\n| 传播行为 | 含义 |\n|---------|------|\n| REQUIRED | 有事务加入，没有新建（默认）|\n| REQUIRES_NEW | 总是新建事务，挂起当前 |\n| NESTED | 嵌套事务，外部回滚影响内部 |\n| SUPPORTS | 有事务加入，没有非事务运行 |\n\n## 事务失效场景\n\n1. `@Transactional` 标注的方法非 public\n2. 同类内部调用（绕过代理）\n3. 异常被 catch 吞掉',
 'Spring IOC 初始化流程、AOP 两种代理方式、七种事务传播行为和常见事务失效场景，面试高频考点。',
 5, 1, 743, 95, 5, 1, 1);


-- =============================================
-- 3. 精选文章（新增50条，覆盖6个分类，总量53条）
-- =============================================

-- 架构设计 (已有1条，再加9条)
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
                                                                                             ('Martin Fowler: Microservices', 'https://martinfowler.com/articles/microservices.html', '架构设计', '微服务架构的定义性文章，Martin Fowler 原作，必读经典', 2),
                                                                                             ('Clean Architecture', 'https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html', '架构设计', 'Uncle Bob 提出的整洁架构，依赖方向始终朝向内层', 3),
                                                                                             ('CQRS 模式详解', 'https://martinfowler.com/bliki/CQRS.html', '架构设计', '命令查询责任分离模式，适用于读写负载差异大的系统', 4),
                                                                                             ('事件驱动架构 (EDA) 指南', 'https://martinfowler.com/articles/201701-event-driven.html', '架构设计', 'Martin Fowler 对事件驱动四种模式的深度分析', 5),
                                                                                             ('API 设计最佳实践', 'https://stackoverflow.blog/2020/03/02/best-practices-for-rest-api-design/', '架构设计', 'Stack Overflow 出品的 REST API 设计规范与实战建议', 6),
                                                                                             ('分布式系统的 CAP 理论', 'https://www.infoq.cn/article/cap-twelve-years-later-how-the-rules-have-changed', '架构设计', 'CAP 理论提出12年后的重新审视，深入理解 C、A、P 的权衡', 7),
                                                                                             ('Netflix 技术博客：混沌工程', 'https://netflixtechblog.com/tagged/chaos-engineering', '架构设计', 'Netflix 混沌工程实践，通过主动注入故障提升系统韧性', 8),
                                                                                             ('Google SRE 手册', 'https://sre.google/sre-book/table-of-contents/', '架构设计', 'Google SRE 完整在线版，SLO/SLI/错误预算等概念的权威来源', 9),
                                                                                             ('高并发系统设计 40 问', 'https://time.geekbang.org/column/intro/100035801', '架构设计', '极客时间出品，系统讲解缓存、消息队列、分库分表等高并发方案', 10),

-- 底层原理 (已有1条，再加8条)
                                                                                             ('深入 Linux 内核：进程调度', 'https://www.kernel.org/doc/html/latest/scheduler/index.html', '底层原理', 'Linux 官方文档，CFS 完全公平调度器的设计与实现', 2),
                                                                                             ('TCP/IP 协议栈图解', 'https://www.ruanyifeng.com/blog/2012/06/internet_protocol_suite_part_i.html', '底层原理', '阮一峰图解 TCP/IP 协议族，通俗易懂，适合系统复习', 3),
                                                                                             ('垃圾收集算法手册', 'https://gchandbook.org/', '底层原理', 'GC 领域最权威的学术参考，覆盖标记清除、引用计数、G1 等所有算法', 4),
                                                                                             ('CPU 缓存与内存屏障', 'https://www.aristeia.com/Papers/DDJ_Jul_Aug_2004_revised.pdf', '底层原理', 'Scott Meyers 经典论文，解释多核 CPU 的缓存一致性与内存可见性', 5),
                                                                                             ('浏览器工作原理详解', 'https://www.html5rocks.com/zh/tutorials/internals/howbrowserswork/', '底层原理', 'Tali Garsiel 深入剖析浏览器渲染引擎，前端开发者必读', 6),
                                                                                             ('操作系统：从程序员视角', 'https://csapp.cs.cmu.edu/', '底层原理', 'CMU CSAPP 官网，深入理解计算机系统，硬核程序员进阶必读', 7),
                                                                                             ('JVM 规范（Java SE 21）', 'https://docs.oracle.com/javase/specs/jvms/se21/html/index.html', '底层原理', 'Oracle 官方 JVM 规范，字节码、类加载、内存模型的最权威参考', 8),
                                                                                             ('V8 引擎工作原理', 'https://v8.dev/blog/elements-kinds', '底层原理', 'V8 官方博客，讲解 JavaScript 对象的内部表示和优化策略', 9),

-- 数据库 (已有1条，再加8条)
                                                                                             ('MySQL 官方文档：索引优化', 'https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html', '数据库', 'MySQL 8.0 官方索引优化指南，包含 EXPLAIN 详细解读', 2),
                                                                                             ('PostgreSQL 与 MySQL 深度对比', 'https://www.enterprisedb.com/blog/postgresql-vs-mysql-360-degree-comparison', '数据库', '全方位对比两大开源数据库的性能、功能、适用场景', 3),
                                                                                             ('Redis 设计与实现', 'http://redisbook.com/', '数据库', '黄健宏著，深入解析 Redis 各数据结构和核心机制的中文资源', 4),
                                                                                             ('数据库事务隔离级别图解', 'https://www.postgresql.org/docs/current/transaction-iso.html', '数据库', 'PostgreSQL 官方文档，图文并茂解释脏读、幻读、不可重复读', 5),
                                                                                             ('ShardingSphere 分库分表实战', 'https://shardingsphere.apache.org/document/current/cn/user-manual/', '数据库', 'Apache ShardingSphere 官方中文文档，分库分表最佳实践', 6),
                                                                                             ('时序数据库选型指南', 'https://db-engines.com/en/ranking/time+series+dbms', '数据库', 'InfluxDB、TimescaleDB、Prometheus 等时序数据库对比与选型', 7),
                                                                                             ('Elasticsearch 权威指南（中文版）', 'https://www.elastic.co/guide/cn/elasticsearch/guide/current/index.html', '数据库', 'Elastic 官方中文翻译版，全文搜索与分析入门到进阶', 8),
                                                                                             ('SQL 性能调优：Execution Plans', 'https://use-the-index-luke.com/', '数据库', '专注于 SQL 索引优化的免费在线书，配有大量执行计划图示', 9),

-- Java（新分类，10条）
                                                                                             ('Effective Java 第3版（中文摘要）', 'https://github.com/clxering/Effective-Java-3rd-edition-Chinese-English-bilingual', '编程语言', 'Effective Java 中英文对照 GitHub 项目，90条最佳实践', 1),
                                                                                             ('Java 并发编程实战（JCIP）', 'https://jcip.net/', '编程语言', 'Brian Goetz 著，Java 并发编程领域最权威的参考书', 2),
                                                                                             ('Oracle Java 官方教程', 'https://docs.oracle.com/javase/tutorial/', '编程语言', 'Oracle 官方 Java 教程，覆盖从基础到高级的完整学习路径', 3),
                                                                                             ('Baeldung：Spring 系列教程', 'https://www.baeldung.com/spring-tutorial', '编程语言', '最好的 Spring 英文教程网站，每篇都有代码示例，持续更新', 4),
                                                                                             ('Java 函数式编程指南', 'https://www.manning.com/books/modern-java-in-action', '编程语言', 'Modern Java in Action，Lambda、Stream、Optional 深度实战', 5),
                                                                                             ('Project Reactor 响应式编程', 'https://projectreactor.io/docs/core/release/reference/', '编程语言', 'WebFlux 底层，Reactor 官方文档，响应式编程入门必读', 6),
                                                                                             ('JMH 基准测试入门', 'https://openjdk.org/projects/code-tools/jmh/', '编程语言', 'OpenJDK 官方基准测试框架，Java 性能测试的标准工具', 7),
                                                                                             ('Arthas 线上诊断工具', 'https://arthas.aliyun.com/doc/', '编程语言', '阿里巴巴开源的 Java 诊断工具，不重启实时排查生产问题', 8),
                                                                                             ('Java 新特性汇总（8~21）', 'https://openjdk.org/projects/jdk/', '编程语言', 'OpenJDK 各版本 JEP 特性列表，Java 8 到 21 新特性速查', 9),
                                                                                             ('JVM 调优实战案例', 'https://heapsampling.blogspot.com/', '编程语言', '真实 JVM 调优案例分析，GC 日志解读与参数优化', 10),

-- 前端（新分类，10条）
                                                                                             ('MDN 前端学习路径', 'https://developer.mozilla.org/zh-CN/docs/Learn', '前端开发', 'Mozilla 官方前端学习路径，HTML/CSS/JS 从零到实战', 1),
                                                                                             ('Vue 3 官方文档（中文）', 'https://cn.vuejs.org/guide/introduction.html', '前端开发', 'Vue 3 官方中文文档，Composition API 最权威的参考', 2),
                                                                                             ('React 官方文档', 'https://react.dev/learn', '前端开发', 'React 新版官方文档，全面使用 Hooks 重写，示例丰富', 3),
                                                                                             ('TypeScript 深入理解', 'https://www.typescriptlang.org/docs/handbook/intro.html', '前端开发', 'TypeScript 官方手册，类型系统、泛型、装饰器完整讲解', 4),
                                                                                             ('Web 性能优化指南', 'https://web.dev/performance/', '前端开发', 'Google web.dev 出品，Core Web Vitals 和性能优化最佳实践', 5),
                                                                                             ('CSS 权威指南', 'https://css-tricks.com/', '前端开发', 'CSS-Tricks，最全面的 CSS 技巧网站，Flexbox/Grid 必备', 6),
                                                                                             ('Vite 官方文档', 'https://cn.vitejs.dev/', '前端开发', 'Vite 中文官方文档，新一代前端构建工具，冷启动极速', 7),
                                                                                             ('JavaScript 事件循环可视化', 'https://www.jsv9000.app/', '前端开发', '交互式可视化 JS 事件循环、任务队列、微任务队列执行过程', 8),
                                                                                             ('WebAssembly 入门', 'https://webassembly.org/getting-started/developers-guide/', '前端开发', 'WebAssembly 官方入门指南，在浏览器中运行 C/Rust 代码', 9),
                                                                                             ('前端工程化：Module Federation', 'https://module-federation.io/', '前端开发', 'Webpack 5 模块联邦，微前端架构实现方案官方文档', 10),

-- 工程实践（新分类，10条）
                                                                                             ('Google 工程实践文档', 'https://google.github.io/eng-practices/', '工程实践', 'Google 官方 Code Review 最佳实践，评审者和作者双视角', 1),
                                                                                             ('The Pragmatic Programmer', 'https://pragprog.com/titles/tpp20/the-pragmatic-programmer-20th-anniversary-edition/', '工程实践', '程序员修炼之道20周年版，每个职业程序员必读的经典', 2),
                                                                                             ('持续集成持续交付（CI/CD）', 'https://www.martinfowler.com/articles/continuousIntegration.html', '工程实践', 'Martin Fowler 关于 CI 的原始论文，理解 CI/CD 的思想基础', 3),
                                                                                             ('代码整洁之道（Clean Code）', 'https://www.oreilly.com/library/view/clean-code-a/9780136083238/', '工程实践', 'Robert C. Martin 著，如何写出可读、可维护的高质量代码', 4),
                                                                                             ('GitHub Actions 工作流', 'https://docs.github.com/zh/actions', '工程实践', 'GitHub Actions 官方中文文档，自动化 CI/CD 流水线配置', 5),
                                                                                             ('测试驱动开发（TDD）实践', 'https://martinfowler.com/bliki/TestDrivenDevelopment.html', '工程实践', 'Martin Fowler 对 TDD 的定义与实践建议，改变编程习惯的方法', 6),
                                                                                             ('监控告警体系建设', 'https://grafana.com/docs/grafana/latest/', '工程实践', 'Grafana 官方文档，从零搭建 Prometheus + Grafana 监控体系', 7),
                                                                                             ('OpenAPI 3.0 规范', 'https://swagger.io/specification/', '工程实践', 'Swagger/OpenAPI 3.0 规范，API 文档标准化的行业标准', 8),
                                                                                             ('技术债务管理实践', 'https://martinfowler.com/bliki/TechnicalDebt.html', '工程实践', 'Martin Fowler 技术债务四象限模型，理性看待和处理技术债', 9),
                                                                                             ('敏捷开发宣言与原则', 'https://agilemanifesto.org/iso/zhchs/manifesto.html', '工程实践', '敏捷宣言中文版，12条原则指导软件开发团队协作与交付', 10);


-- =============================================
-- 4. 学习网址（新增22条，总量30条）
-- =============================================
INSERT INTO `website_nav` (`name`, `url`, `description`, `category`, `sort_order`) VALUES
-- 开发工具（原有2条，再加4条）
('Regex101',         'https://regex101.com',            '在线正则表达式调试工具，支持多语言',        '开发工具', 3),
('JSON Formatter',   'https://jsonformatter.curiousconcept.com', 'JSON 格式化、验证、对比工具',   '开发工具', 4),
('cron 表达式生成',   'https://crontab.guru',             '在线 cron 表达式生成与解析工具',            '开发工具', 5),
('carbon',           'https://carbon.now.sh',            '将代码生成精美截图，适合分享到社交媒体',    '开发工具', 6),

-- 文档手册（原有3条，再加5条）
('Redis 官方文档',   'https://redis.io/docs/',           'Redis 官方文档，命令参考和最佳实践',         '文档手册', 4),
('Docker 文档',      'https://docs.docker.com',          'Docker 官方文档，从入门到生产部署',          '文档手册', 5),
('Kubernetes 文档',  'https://kubernetes.io/zh-cn/docs', 'K8s 中文官方文档，容器编排权威参考',         '文档手册', 6),
('MyBatis 文档',     'https://mybatis.org/mybatis-3/zh', 'MyBatis 官方中文文档，SQL 映射框架',         '文档手册', 7),
('Linux man 手册',   'https://man7.org/linux/man-pages', 'Linux 命令行手册在线版，权威参考',           '文档手册', 8),

-- 技术社区（原有1条，再加4条）
('InfoQ',            'https://www.infoq.cn',             '软件开发领域最具影响力的技术媒体',           '技术社区', 2),
('CSDN',             'https://www.csdn.net',             '中国最大的 IT 技术社区，海量技术博客',        '技术社区', 3),
('V2EX',             'https://v2ex.com',                 '面向程序员的创意工作者社区',                  '技术社区', 4),
('SegmentFault',     'https://segmentfault.com',         '思否，专注于解决编程问题的技术问答社区',      '技术社区', 5),

-- 学习资源（原有2条，再加5条）
('MIT OpenCourseWare','https://ocw.mit.edu',             'MIT 免费公开课，计算机科学顶级课程资源',     '学习资源', 3),
('Coursera',         'https://www.coursera.org',         '全球顶级在线学习平台，提供名校认证课程',     '学习资源', 4),
('CS自学指南',        'https://csdiy.wiki',              '北大信科学长整理的计算机自学路径推荐',       '学习资源', 5),
('OI Wiki',          'https://oi-wiki.org',              '算法竞赛知识整合网站，数据结构与算法必备',   '学习资源', 6),
('系统设计面试',      'https://github.com/donnemartin/system-design-primer', '系统设计面试准备，GitHub 25万+ Star', '学习资源', 7),

-- 新分类：AI 工具
('ChatGPT',          'https://chat.openai.com',          'OpenAI 官方 AI 对话助手',                    'AI 工具', 1),
('GitHub Copilot',   'https://github.com/features/copilot', 'AI 代码补全工具，提升编码效率',           'AI 工具', 2),
('Hugging Face',     'https://huggingface.co',           'AI 模型与数据集托管平台，开源 AI 社区',      'AI 工具', 3),
('Perplexity AI',    'https://www.perplexity.ai',        '基于 AI 的搜索引擎，附带来源引用',           'AI 工具', 4);


-- =============================================
-- 5. 新文章的评论
-- =============================================
INSERT INTO `comment` (`article_id`, `parent_id`, `content`, `nickname`, `email`) VALUES
-- JVM 文章（id=6）
(6, 0, '终于搞懂了为什么 G1 比 CMS 好！文章把 Eden、Survivor 讲得很清楚。', '小明', 'xm@qq.com'),
(6, 0, '请问生产环境推荐用 G1 还是 ZGC？我们服务要求低延迟。', '运维小哥', NULL),
-- Docker 文章（id=7）
(7, 0, '刚好在搞容器化，收藏了！docker-compose 那段正好是我需要的。', '云原生学习者', 'cloud@163.com'),
-- 分布式锁文章（id=10）
(10, 0, 'Redisson 的看门狗机制是亮点，之前自己写总会有锁过期的问题。', '后端老兵', NULL),
(10, 0, '分布式锁还有 ZooKeeper 方案，什么情况下选 ZK 比 Redis 好？', '架构新人', 'arch@example.com'),
(10, 4, 'ZK 锁强一致性更好，但性能低于 Redis。业务不要求强一致用 Redis，否则用 ZK。', '博主', 'admin@blog.com'),
-- Java集合面试（id=13）
(13, 0, '面试刚考了 ConcurrentHashMap，这篇来得太及时了！', '找工作中', NULL),
(13, 0, 'LRU 用 LinkedHashMap 实现那里能不能展开说说？', '算法练习生', 'algo@example.com'),
(13, 0, '补充：JDK21 的 SequencedCollection 接口也可以了解一下。', 'Java老司机', NULL),
(13, 9, '好提议，下篇文章专门讲 JDK 新特性！', '博主', 'admin@blog.com'),
-- 读书方法（id=11）
(11, 0, '费曼学习法我也在用，写博客就是最好的实践方式。', '学习爱好者', 'learn@qq.com'),
(11, 0, '《DDIA》确实神书，我看了三遍还没完全消化。', '分布式学徒', NULL);

-- 更新评论计数
UPDATE `article` SET `comment_count` = 2 WHERE `id` = 6;
UPDATE `article` SET `comment_count` = 1 WHERE `id` = 7;
UPDATE `article` SET `comment_count` = 3 WHERE `id` = 10;
UPDATE `article` SET `comment_count` = 4 WHERE `id` = 13;
UPDATE `article` SET `comment_count` = 2 WHERE `id` = 11;


-- 精选文章：架构设计 补充25条（当前10条，共35条，可测试多页分页）
INSERT INTO `featured_article` (`title`, `url`, `category`, `description`, `sort_order`) VALUES
                                                                                             ('领域驱动设计（DDD）入门', 'https://martinfowler.com/tags/domain%20driven%20design.html', '架构设计', 'Martin Fowler 关于 DDD 系列文章汇总，战略设计与战术设计', 11),
                                                                                             ('六边形架构（端口与适配器）', 'https://alistair.cockburn.us/hexagonal-architecture/', '架构设计', 'Alistair Cockburn 原文，将核心业务与外部系统彻底隔离的架构模式', 12),
                                                                                             ('Saga 分布式事务模式', 'https://microservices.io/patterns/data/saga.html', '架构设计', 'Chris Richardson 微服务模式网站，Saga 协调式与编排式详解', 13),
                                                                                             ('服务网格（Service Mesh）原理', 'https://istio.io/latest/docs/concepts/what-is-istio/', '架构设计', 'Istio 官方概念文档，理解 Service Mesh 的流量管理与可观测性', 14),
                                                                                             ('背压（Backpressure）机制详解', 'https://www.reactivemanifesto.org/', '架构设计', '响应式宣言，异步系统中背压的重要性与实现方式', 15),
                                                                                             ('数据库分片（Sharding）策略', 'https://use-the-index-luke.com/sql/partitioning', '架构设计', '水平分片、垂直分片的选择依据与常见分片键设计陷阱', 16),
                                                                                             ('幂等性设计：接口重试的正确姿势', 'https://aws.amazon.com/builders-library/making-retries-safe-with-idempotent-APIs/', '架构设计', 'AWS Builder Library，幂等 API 设计防止重复扣款、重复下单', 17),
                                                                                             ('Rate Limiting 限流算法对比', 'https://konghq.com/blog/how-to-design-a-scalable-rate-limiting-algorithm', '架构设计', '令牌桶、漏桶、滑动窗口四种限流算法原理与适用场景对比', 18),
                                                                                             ('蓝绿部署与金丝雀发布', 'https://martinfowler.com/bliki/BlueGreenDeployment.html', '架构设计', 'Martin Fowler 蓝绿部署与 Canary Release 原文，零停机发布方案', 19),
                                                                                             ('API Gateway 设计模式', 'https://microservices.io/patterns/apigateway.html', '架构设计', '微服务 API 网关模式，解决客户端多服务调用的聚合与路由问题', 20),
                                                                                             ('断路器（Circuit Breaker）模式', 'https://martinfowler.com/bliki/CircuitBreaker.html', '架构设计', 'Martin Fowler 断路器原文，防止级联故障蔓延的核心容错模式', 21),
                                                                                             ('SOLID 原则实战解析', 'https://blog.cleancoder.com/uncle-bob/2020/10/18/Solid-Relevance.html', '架构设计', 'Uncle Bob 亲解 SOLID 在现代软件开发中的意义与误区', 22),
                                                                                             ('事件溯源（Event Sourcing）', 'https://martinfowler.com/eaaDev/EventSourcing.html', '架构设计', '以事件日志作为数据存储的核心模式，配合 CQRS 构建可审计系统', 23),
                                                                                             ('Strangler Fig：遗留系统改造', 'https://martinfowler.com/bliki/StranglerFigApplication.html', '架构设计', '勒死无花果树模式，渐进式替换遗留系统而非一次性重写', 24),
                                                                                             ('两阶段提交（2PC）与 3PC', 'https://www.cs.princeton.edu/courses/archive/fall16/cos418/docs/L6-2pc.html', '架构设计', 'Princeton 分布式课程讲义，2PC 协议原理与阻塞问题分析', 25),
                                                                                             ('一致性哈希算法', 'https://tom-e-white.com/2007/11/consistent-hashing-and-random-trees.html', '架构设计', '一致性哈希原始论文，解决分布式缓存节点增减时的数据迁移问题', 26),
                                                                                             ('读写分离架构实践', 'https://dev.mysql.com/doc/refman/8.0/en/replication.html', '架构设计', 'MySQL 主从复制官方文档，读写分离架构的数据一致性保障', 27),
                                                                                             ('Outbox Pattern：事务性消息发送', 'https://microservices.io/patterns/data/transactional-outbox.html', '架构设计', '解决数据库写入与消息发送原子性问题的 Outbox 模式详解', 28),
                                                                                             ('云原生应用设计原则', 'https://www.cncf.io/blog/2019/08/22/how-do-cloud-native-and-kubernetes-serve-as-the-foundation-of-5g/', '架构设计', 'CNCF 云原生基金会，容器化、动态编排、微服务的设计理念', 29),
                                                                                             ('eBay 亿级数据架构演进', 'https://www.ebayinc.com/stories/blogs/tech/architecture-evolution/', '架构设计', 'eBay 技术博客，从单体到分布式的真实架构演进历程', 30),
                                                                                             ('Twitter 时间线架构设计', 'https://highscalability.com/blog/2013/7/8/the-architecture-twitter-uses-to-deal-with-150m-active-users.html', '架构设计', 'High Scalability 解析 Twitter 1.5亿用户的 Feed 流分发架构', 31),
                                                                                             ('Pinterest 从零到百亿 PV', 'https://highscalability.com/blog/2013/4/15/scaling-pinterest-from-0-to-10s-of-billions-of-page-views-a.html', '架构设计', 'Pinterest 从 0 到百亿 PV 的数据库拆分与缓存演进实录', 32),
                                                                                             ('Uber 实时系统设计', 'https://eng.uber.com/real-time-exactly-once-ad-event-processing/', '架构设计', 'Uber 工程博客，Exactly-Once 语义在实时广告事件处理中的实现', 33),
                                                                                             ('Discord 从 MongoDB 迁移至 Cassandra', 'https://discord.com/blog/how-discord-stores-billions-of-messages', '架构设计', 'Discord 如何存储数十亿条消息，NoSQL 选型与数据建模实践', 34),
                                                                                             ('Cloudflare 边缘计算架构', 'https://blog.cloudflare.com/cloudflare-workers-the-architecture/', '架构设计', 'Cloudflare Workers 架构解析，边缘节点无服务器计算的实现原理', 35);

INSERT INTO `interview` (`title`, `content`, `summary`, `category_id`, `author_id`, `view_count`, `like_count`, `comment_count`, `status`, `is_top`, `create_time`, `update_time`) VALUES

                                                                                                                                                                                       ('HashMap底层原理与扩容机制详解',
                                                                                                                                                                                        '# HashMap底层原理与扩容机制详解\n\n## 面试题目\n\n> 请描述 HashMap 的底层数据结构，以及扩容机制是怎么实现的？\n\n## 数据结构\n\nJDK 1.8 之前，HashMap 采用 **数组 + 链表** 的结构。JDK 1.8 之后改为 **数组 + 链表 + 红黑树**。\n\n```java\n// Node 节点结构\nstatic class Node<K,V> implements Map.Entry<K,V> {\n    final int hash;\n    final K key;\n    V value;\n    Node<K,V> next; // 链表指针\n}\n```\n\n## 核心参数\n\n| 参数 | 默认值 | 说明 |\n|------|--------|------|\n| 初始容量 | 16 | 必须是2的幂 |\n| 负载因子 | 0.75 | 扩容阈值 = 容量 × 负载因子 |\n| 树化阈值 | 8 | 链表长度 ≥ 8 且数组长度 ≥ 64 时转红黑树 |\n| 退化阈值 | 6 | 红黑树节点数 ≤ 6 时退回链表 |\n\n## put 流程\n\n1. 计算 key 的 hash 值：`hash = key.hashCode() ^ (key.hashCode() >>> 16)`（高低16位异或，减少碰撞）\n2. 通过 `(n-1) & hash` 确定数组下标\n3. 若该位置为空，直接插入\n4. 若已有元素，判断 key 是否相同（先比较 hash，再 equals）\n5. 链表长度超过阈值转红黑树\n6. 插入后若 `size > threshold` 触发扩容\n\n## 扩容机制\n\n扩容时容量变为原来的 **2倍**，然后 rehash 所有元素。\n\n```java\n// 元素在新数组中的位置只有两种情况：\n// 1. 原位置不变（高位为0）\n// 2. 原位置 + 旧容量（高位为1）\nif ((e.hash & oldCap) == 0)\n    低位链表（原位置）\nelse\n    高位链表（原位置 + oldCap）\n```\n\n这样设计避免了重新计算 hash，效率更高。\n\n## 为什么容量必须是2的幂？\n\n`index = hash & (n-1)`，当 n 为2的幂时，`n-1` 的二进制全为1，等价于取模运算，且位运算效率更高。\n\n## 线程安全问题\n\n- JDK 1.7 并发扩容会导致**链表死循环**\n- JDK 1.8 改为尾插法解决了死循环，但仍存在数据丢失问题\n- 多线程场景应使用 `ConcurrentHashMap`',
                                                                                                                                                                                        '深入分析HashMap底层数组+链表+红黑树结构，put流程，扩容时容量翻倍并利用高位bit判断新位置，以及为何容量必须是2的幂。',
                                                                                                                                                                                        1, 1, 1823, 156, 12, 1, 1, '2026-01-05 09:00:00', '2026-01-05 09:00:00'),

                                                                                                                                                                                       ('ConcurrentHashMap如何实现线程安全',
                                                                                                                                                                                        '# ConcurrentHashMap如何实现线程安全\n\n## 面试题目\n\n> ConcurrentHashMap 是如何保证线程安全的？JDK 1.7 和 1.8 有何区别？\n\n## JDK 1.7 — Segment 分段锁\n\n```\nConcurrentHashMap\n├── Segment[0]  (ReentrantLock)\n│   └── HashEntry[]\n├── Segment[1]  (ReentrantLock)\n│   └── HashEntry[]\n...\n└── Segment[15] (ReentrantLock)\n    └── HashEntry[]\n```\n\n默认16个Segment，每个Segment相当于一个小HashMap并继承ReentrantLock，不同Segment可以并发操作，最大并发度为16。\n\n## JDK 1.8 — CAS + synchronized\n\nJDK 1.8 抛弃了Segment，改用更细粒度的锁：\n\n```java\nfinal V putVal(K key, V value, boolean onlyIfAbsent) {\n    // 1. 数组槽位为空：CAS 无锁插入\n    if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value)))\n        break;\n    // 2. 槽位有元素：synchronized 锁住链表/红黑树头节点\n    synchronized (f) {\n        // 遍历链表或红黑树插入\n    }\n}\n```\n\n**锁粒度从 Segment 细化到了单个数组槽位的头节点**，并发度大幅提升。\n\n## size() 的实现\n\n通过 `baseCount + CounterCell[]` 分段计数，类似 LongAdder，减少 CAS 竞争。\n\n## get() 为何不需要加锁？\n\n- 数组引用用 `volatile` 修饰，保证可见性\n- Node 的 `val` 和 `next` 也是 `volatile`，读取时无需加锁\n\n## 对比总结\n\n| 特性 | JDK 1.7 | JDK 1.8 |\n|------|---------|----------|\n| 数据结构 | Segment + HashEntry | 数组 + 链表 + 红黑树 |\n| 锁机制 | ReentrantLock (Segment级) | synchronized (Node级) |\n| 最大并发 | Segment数量(默认16) | 数组长度 |\n| 树化 | 不支持 | 链表长度≥8转红黑树 |',
                                                                                                                                                                                        'JDK1.7使用Segment分段锁，JDK1.8改用CAS+synchronized锁住头节点，锁粒度更细，并发度更高，同时get()无需加锁因为用volatile保证可见性。',
                                                                                                                                                                                        1, 1, 1456, 132, 8, 1, 0, '2026-01-06 10:00:00', '2026-01-06 10:00:00'),

                                                                                                                                                                                       ('volatile关键字的内存语义与适用场景',
                                                                                                                                                                                        '# volatile关键字的内存语义与适用场景\n\n## 面试题目\n\n> volatile 能保证原子性吗？它的底层原理是什么？\n\n## 两大语义\n\n### 1. 可见性\n\n对 volatile 变量的写操作会立即刷新到主内存，其他线程读取时会从主内存重新加载，而不是使用CPU缓存中的旧值。\n\n### 2. 禁止指令重排序\n\n通过插入**内存屏障（Memory Barrier）**实现：\n\n```\n写操作：StoreStore屏障 → volatile写 → StoreLoad屏障\n读操作：LoadLoad屏障 → volatile读 → LoadStore屏障\n```\n\n## 不能保证原子性\n\n```java\nprivate volatile int count = 0;\n\n// 以下操作不是原子的！\ncount++; // 等价于：读count → 加1 → 写count（三步操作）\n```\n\n多线程并发 `count++` 仍然会出现数据丢失。应使用 `AtomicInteger`。\n\n## 底层实现\n\n编译为汇编指令时会插入 **`lock` 前缀指令**：\n\n1. 将当前CPU缓存行数据写回主内存\n2. 使其他CPU中该内存地址的缓存行无效（MESI协议）\n\n## 经典使用场景\n\n### 状态标志\n```java\nprivate volatile boolean running = true;\n\npublic void stop() { running = false; }\n\npublic void run() {\n    while (running) { /* 工作 */ }\n}\n```\n\n### 双重检查锁单例（DCL）\n```java\npublic class Singleton {\n    private static volatile Singleton instance;\n\n    public static Singleton getInstance() {\n        if (instance == null) {                    // 第一次检查\n            synchronized (Singleton.class) {\n                if (instance == null) {            // 第二次检查\n                    instance = new Singleton();    // 禁止重排序！\n                }\n            }\n        }\n        return instance;\n    }\n}\n```\n\n`instance = new Singleton()` 包含三步：分配内存→初始化对象→赋值引用，不加 volatile 可能发生重排序导致其他线程拿到未初始化的对象。',
                                                                                                                                                                                        'volatile保证可见性和禁止指令重排序，但不保证原子性。底层通过lock前缀指令+MESI协议实现。适用于状态标志和DCL单例模式。',
                                                                                                                                                                                        1, 1, 1234, 98, 6, 1, 0, '2026-01-07 09:30:00', '2026-01-07 09:30:00'),

                                                                                                                                                                                       ('synchronized底层原理与锁升级过程',
                                                                                                                                                                                        '# synchronized底层原理与锁升级过程\n\n## 面试题目\n\n> synchronized 是怎么实现的？锁升级过程是怎样的？\n\n## 字节码层面\n\n```java\nsynchronized(obj) { /* ... */ }\n```\n\n编译后对应 `monitorenter` 和 `monitorexit` 字节码指令，依赖对象头中的 **Mark Word** 和 **Monitor（管程）**。\n\n## 对象头 Mark Word\n\n```\n64位JVM中 Mark Word 结构（64bit）：\n├── 无锁态：hashCode(31) + 分代年龄(4) + 偏向锁标志(1) + 锁标志(2=01)\n├── 偏向锁：线程ID(54) + epoch(2) + 分代年龄(4) + 1 + 01\n├── 轻量级锁：指向栈中锁记录的指针(62) + 00\n└── 重量级锁：指向Monitor的指针(62) + 10\n```\n\n## 锁升级过程\n\n```\n无锁 → 偏向锁 → 轻量级锁 → 重量级锁\n```\n\n### 偏向锁\n只有一个线程访问时，将线程ID记入Mark Word，下次同一线程进入无需任何同步操作。\n\n### 轻量级锁\n出现第二个线程竞争时，偏向锁撤销，升级为轻量级锁。线程通过**CAS**将对象头的Mark Word替换为指向自己栈帧中锁记录的指针，失败则自旋等待。\n\n### 重量级锁\n自旋超过一定次数（自适应自旋）后，升级为重量级锁，线程进入**操作系统内核态**阻塞，依赖OS的mutex互斥量实现。\n\n## synchronized vs ReentrantLock\n\n| 特性 | synchronized | ReentrantLock |\n|------|-------------|---------------|\n| 实现层面 | JVM内置 | JDK API |\n| 锁释放 | 自动释放 | 必须手动unlock |\n| 可中断 | 不支持 | 支持 lockInterruptibly |\n| 公平锁 | 不支持 | 支持 |\n| 多条件 | 一个waitSet | 多个Condition |\n| 性能 | JDK6后差距不大 | 高竞争下稍好 |',
                                                                                                                                                                                        'synchronized通过对象头Mark Word和Monitor实现，经历无锁→偏向锁→轻量级锁(CAS自旋)→重量级锁(OS互斥量)的升级过程。与ReentrantLock的对比也是高频考点。',
                                                                                                                                                                                        1, 1, 1678, 145, 11, 1, 0, '2026-01-08 14:00:00', '2026-01-08 14:00:00'),

                                                                                                                                                                                       ('JVM内存区域与垃圾回收算法',
                                                                                                                                                                                        '# JVM内存区域与垃圾回收算法\n\n## 面试题目\n\n> JVM运行时内存分哪几个区域？常见的垃圾回收算法有哪些？\n\n## 运行时数据区\n\n```\nJVM Memory\n├── 线程共享\n│   ├── 堆（Heap）         — 对象实例、数组，GC主战场\n│   └── 方法区（Metaspace）— 类信息、常量、静态变量\n└── 线程私有\n    ├── 程序计数器          — 当前字节码行号，唯一不OOM的区域\n    ├── 虚拟机栈            — 栈帧（局部变量、操作数栈、动态链接）\n    └── 本地方法栈          — Native方法\n```\n\n> JDK 8 将永久代（PermGen）替换为**元空间（Metaspace）**，使用本地内存，避免 OOM。\n\n## 垃圾回收算法\n\n### 标记-清除（Mark-Sweep）\n- 标记存活对象，然后清除未标记对象\n- **缺点**：产生大量内存碎片\n\n### 标记-复制（Copying）\n- 将内存分为两块，存活对象复制到另一块，清除当前块\n- **优点**：无碎片；**缺点**：内存利用率只有50%\n- **用于新生代**（Eden:S0:S1 = 8:1:1）\n\n### 标记-整理（Mark-Compact）\n- 标记后将存活对象移动到一端，清除边界外的内存\n- **优点**：无碎片且内存利用率高；**缺点**：移动对象开销大\n- **用于老年代**\n\n## 分代回收\n\n```\nHeap\n├── Young Generation（新生代）\n│   ├── Eden（80%）       — 新对象在此分配\n│   ├── Survivor 0（10%）\n│   └── Survivor 1（10%）\n└── Old Generation（老年代）— 长期存活对象晋升至此\n```\n\n- **Minor GC**：回收新生代，频繁但快速\n- **Major/Full GC**：回收老年代（+新生代），慢，需STW\n\n## 常用垃圾收集器\n\n| 收集器 | 区域 | 算法 | 特点 |\n|--------|------|------|------|\n| Serial | 新生代 | 复制 | 单线程，STW |\n| Parallel Scavenge | 新生代 | 复制 | 多线程，吞吐量优先 |\n| CMS | 老年代 | 标记-清除 | 并发，低停顿 |\n| G1 | 全堆 | 标记-整理+复制 | 可预测停顿，JDK9+默认 |\n| ZGC | 全堆 | 染色指针 | 停顿<10ms |',
                                                                                                                                                                                        'JVM运行时分为堆、方法区（Metaspace）、程序计数器、虚拟机栈、本地方法栈。GC算法包括标记-清除、标记-复制（新生代）、标记-整理（老年代），主流收集器G1和ZGC是重点。',
                                                                                                                                                                                        1, 1, 2103, 189, 15, 1, 1, '2026-01-09 10:00:00', '2026-01-09 10:00:00'),

                                                                                                                                                                                       ('Spring Bean的生命周期',
                                                                                                                                                                                        '# Spring Bean的生命周期\n\n## 面试题目\n\n> 请描述Spring Bean完整的生命周期，越详细越好。\n\n## 完整生命周期流程\n\n```\n1. 实例化（Instantiation）\n   └── 通过反射调用构造方法创建对象\n\n2. 属性填充（Populate Properties）\n   └── 依赖注入（@Autowired、@Value、setter注入）\n\n3. 初始化前处理\n   ├── BeanNameAware.setBeanName()\n   ├── BeanFactoryAware.setBeanFactory()\n   └── ApplicationContextAware.setApplicationContext()\n\n4. BeanPostProcessor.postProcessBeforeInitialization()\n   └── AOP代理在这一步或下一步生成\n\n5. 初始化（Initialization）\n   ├── @PostConstruct 方法\n   ├── InitializingBean.afterPropertiesSet()\n   └── init-method 属性指定的方法\n\n6. BeanPostProcessor.postProcessAfterInitialization()\n   └── AOP代理最终在这里完成\n\n7. Bean就绪，放入IoC容器使用\n\n8. 销毁（Destruction）\n   ├── @PreDestroy 方法\n   ├── DisposableBean.destroy()\n   └── destroy-method 属性指定的方法\n```\n\n## 关键扩展点示例\n\n```java\n@Component\npublic class MyBean implements BeanNameAware, InitializingBean, DisposableBean {\n\n    @PostConstruct\n    public void init() {\n        System.out.println("@PostConstruct");\n    }\n\n    @Override\n    public void afterPropertiesSet() {\n        System.out.println("InitializingBean.afterPropertiesSet()");\n    }\n\n    @PreDestroy\n    public void destroy() {\n        System.out.println("@PreDestroy");\n    }\n}\n```\n\n## 执行顺序记忆口诀\n\n> 构造 → 注入 → Aware → Before → PostConstruct → afterPropertiesSet → init-method → After → 使用 → PreDestroy → destroy → destroy-method\n\n## 面试追问：BeanPostProcessor 的作用\n\n`BeanPostProcessor` 是Spring AOP的实现基础。`AnnotationAwareAspectJAutoProxyCreator` 就是一个 BPP，在 `postProcessAfterInitialization` 中检查是否需要为Bean创建代理对象。',
                                                                                                                                                                                        'Spring Bean生命周期：实例化→属性注入→Aware接口→BPP前置处理→@PostConstruct→afterPropertiesSet→BPP后置处理→使用→@PreDestroy→destroy销毁。BeanPostProcessor是AOP的实现基础。',
                                                                                                                                                                                        1, 1, 1945, 167, 13, 1, 0, '2026-01-10 09:00:00', '2026-01-10 09:00:00'),

                                                                                                                                                                                       ('Spring AOP底层原理：JDK动态代理vs CGLIB',
                                                                                                                                                                                        '# Spring AOP底层原理：JDK动态代理vs CGLIB\n\n## 面试题目\n\n> Spring AOP 底层是如何实现的？JDK代理和CGLIB代理有何区别？\n\n## AOP核心概念\n\n| 概念 | 说明 |\n|------|------|\n| Aspect | 切面，封装横切逻辑的类（@Aspect） |\n| Pointcut | 切入点，定义在哪些方法上织入（@Pointcut） |\n| Advice | 通知，具体的横切逻辑（@Before/@After/@Around） |\n| JoinPoint | 连接点，程序执行的某个位置 |\n| Weaving | 织入，将切面应用到目标对象的过程 |\n\n## 两种代理方式\n\n### JDK 动态代理\n- **条件**：目标类实现了接口\n- **原理**：通过 `Proxy.newProxyInstance()` 生成接口的代理类\n- **核心**：实现 `InvocationHandler.invoke()` 方法\n\n```java\nObject proxy = Proxy.newProxyInstance(\n    target.getClass().getClassLoader(),\n    target.getClass().getInterfaces(),\n    (proxy, method, args) -> {\n        // 前置逻辑\n        Object result = method.invoke(target, args);\n        // 后置逻辑\n        return result;\n    }\n);\n```\n\n### CGLIB 代理\n- **条件**：目标类未实现接口（或强制使用CGLIB）\n- **原理**：通过字节码技术动态生成目标类的**子类**\n- **限制**：无法代理 `final` 类和 `final` 方法\n\n## Spring的选择策略\n\n```\nif (目标类实现了接口 && !proxyTargetClass)\n    使用 JDK 动态代理\nelse\n    使用 CGLIB 代理\n```\n\nSpringBoot 2.x 默认强制使用 CGLIB（`spring.aop.proxy-target-class=true`）。\n\n## @Transactional 失效场景\n\n基于AOP代理，以下场景会导致事务失效：\n1. **同类内方法调用**：`this.methodB()` 不走代理，事务不生效\n2. **方法非public**：CGLIB/JDK代理都无法拦截\n3. **异常被catch吞掉**：事务不知道需要回滚\n4. **非受检异常配置错误**：默认只回滚 `RuntimeException`',
                                                                                                                                                                                        'Spring AOP通过JDK动态代理（目标类有接口）或CGLIB（生成子类）实现。SpringBoot默认强制CGLIB。同类方法调用会绕过代理导致@Transactional失效是高频考点。',
                                                                                                                                                                                        1, 1, 1567, 143, 10, 1, 0, '2026-01-11 14:00:00', '2026-01-11 14:00:00'),

                                                                                                                                                                                       ('Spring事务传播行为7种详解',
                                                                                                                                                                                        '# Spring事务传播行为7种详解\n\n## 面试题目\n\n> Spring事务的传播行为有哪些？REQUIRED 和 REQUIRES_NEW 有什么区别？\n\n## 7种传播行为\n\n| 传播类型 | 说明 |\n|----------|------|\n| **REQUIRED**（默认） | 有事务则加入，无则新建 |\n| **REQUIRES_NEW** | 总是新建事务，暂停外部事务 |\n| **SUPPORTS** | 有事务则加入，无则以非事务方式执行 |\n| **NOT_SUPPORTED** | 以非事务方式执行，有外部事务则暂停 |\n| **MANDATORY** | 必须在事务中执行，否则抛异常 |\n| **NEVER** | 以非事务方式执行，有事务则抛异常 |\n| **NESTED** | 有事务则在嵌套事务中执行（Savepoint） |\n\n## REQUIRED vs REQUIRES_NEW\n\n```java\n@Service\npublic class OrderService {\n\n    @Autowired\n    private LogService logService;\n\n    @Transactional  // REQUIRED\n    public void createOrder() {\n        // 下单逻辑...\n        logService.saveLog(); // 被调用方传播行为影响结果\n    }\n}\n```\n\n**REQUIRED（加入同一事务）**：\n- `createOrder` 和 `saveLog` 在同一事务\n- 任一方抛异常，整个事务回滚\n\n**REQUIRES_NEW（独立事务）**：\n- `saveLog` 开启独立事务\n- `saveLog` 提交后，`createOrder` 回滚不影响日志\n- 适用于：**日志记录、消息发送等不应被主事务影响的操作**\n\n## NESTED vs REQUIRES_NEW\n\n| 特性 | NESTED | REQUIRES_NEW |\n|------|--------|---------------|\n| 事务数量 | 1个（嵌套） | 2个（独立） |\n| 外部回滚 | 内部一起回滚 | 内部不受影响 |\n| 内部回滚 | 回滚到Savepoint | 独立回滚 |\n| 数据库支持 | 需要支持Savepoint | 通用 |',
                                                                                                                                                                                        'Spring7种事务传播行为中REQUIRED最常用，REQUIRES_NEW用于独立操作（如日志），NESTED基于Savepoint实现嵌套事务。REQUIRED和REQUIRES_NEW的区别是面试重点。',
                                                                                                                                                                                        1, 1, 1234, 112, 7, 1, 0, '2026-01-12 10:00:00', '2026-01-12 10:00:00'),

                                                                                                                                                                                       ('SpringBoot自动装配原理',
                                                                                                                                                                                        '# SpringBoot自动装配原理\n\n## 面试题目\n\n> SpringBoot是如何实现自动装配的？@SpringBootApplication 注解做了什么？\n\n## @SpringBootApplication 组合注解\n\n```java\n@SpringBootConfiguration   // 等同于 @Configuration\n@EnableAutoConfiguration   // 核心：开启自动装配\n@ComponentScan             // 扫描当前包及子包\npublic @interface SpringBootApplication {}\n```\n\n## 自动装配核心流程\n\n### 1. @EnableAutoConfiguration\n\n```java\n@Import(AutoConfigurationImportSelector.class)\npublic @interface EnableAutoConfiguration {}\n```\n\n### 2. AutoConfigurationImportSelector\n\n```java\n// SpringBoot 2.7 之前\nList<String> configurations = SpringFactoriesLoader.loadFactoryNames(\n    EnableAutoConfiguration.class,\n    classLoader\n); // 读取 META-INF/spring.factories\n\n// SpringBoot 2.7+\n// 读取 META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports\n```\n\n### 3. 条件化装配（按需加载）\n\n```java\n@Configuration\n@ConditionalOnClass(DataSource.class)        // 类路径有DataSource才生效\n@ConditionalOnMissingBean(DataSource.class)  // 容器中没有才生效\n@ConditionalOnProperty(prefix = "spring.datasource", name = "url") // 配置存在才生效\npublic class DataSourceAutoConfiguration {\n    @Bean\n    public DataSource dataSource() { ... }\n}\n```\n\n## 完整流程图\n\n```\n@SpringBootApplication\n  → @EnableAutoConfiguration\n    → AutoConfigurationImportSelector\n      → 读取 spring.factories / AutoConfiguration.imports\n        → 加载所有AutoConfiguration类\n          → @Conditional 过滤不满足条件的配置\n            → 将满足条件的Bean注册到容器\n```\n\n## 面试回答思路\n\n1. `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`\n2. `@EnableAutoConfiguration` 通过 `AutoConfigurationImportSelector` 读取 `spring.factories` 中的配置类列表\n3. 这些配置类通过 `@Conditional` 系列注解按需加载，不是全部加载\n4. 这就是为什么引入某个starter后，相关Bean就自动注册了',
                                                                                                                                                                                        '@SpringBootApplication包含@EnableAutoConfiguration，通过AutoConfigurationImportSelector读取spring.factories加载配置类，再通过@Conditional系列注解按需装配，实现开箱即用。',
                                                                                                                                                                                        1, 1, 1789, 155, 9, 1, 0, '2026-01-13 09:00:00', '2026-01-13 09:00:00'),

                                                                                                                                                                                       ('MySQL索引类型与最左前缀原则',
                                                                                                                                                                                        '# MySQL索引类型与最左前缀原则\n\n## 面试题目\n\n> MySQL中有哪些索引类型？联合索引的最左前缀原则是什么？\n\n## 索引数据结构\n\n**为什么用B+树而不是B树？**\n\n| 特性 | B树 | B+树 |\n|------|-----|------|\n| 数据存储 | 所有节点存数据 | 只有叶子节点存数据 |\n| 叶子节点 | 无链表 | 双向链表相连 |\n| 范围查询 | 需要中序遍历 | 直接遍历链表，高效 |\n| 磁盘IO | 较多 | 非叶节点只存key，单页存更多，IO更少 |\n\n## 索引分类\n\n```sql\n-- 主键索引（聚簇索引）：叶子节点存完整行数据\nPRIMARY KEY (id)\n\n-- 二级索引（辅助索引）：叶子节点存主键值，需回表查询\nINDEX idx_name (name)\n\n-- 联合索引：多列组合\nINDEX idx_name_age (name, age)\n\n-- 覆盖索引：查询列都在索引中，无需回表\nSELECT name, age FROM t WHERE name = \'Tom\' -- 使用 idx_name_age 直接返回\n\n-- 唯一索引\nUNIQUE INDEX uk_email (email)\n```\n\n## 最左前缀原则\n\n对于联合索引 `(a, b, c)`：\n\n```sql\n-- ✅ 走索引\nWHERE a = 1\nWHERE a = 1 AND b = 2\nWHERE a = 1 AND b = 2 AND c = 3\nWHERE a = 1 AND b > 2          -- a走索引，b走索引范围查询，c失效\n\n-- ❌ 不走索引（未从最左列开始）\nWHERE b = 2\nWHERE b = 2 AND c = 3\n\n-- ❌ 跳列\nWHERE a = 1 AND c = 3          -- a走，c不走\n```\n\n## 索引失效场景\n\n```sql\n-- 1. 对索引列做运算或函数\nWHERE YEAR(create_time) = 2024\n\n-- 2. 隐式类型转换（字段是varchar，传入int）\nWHERE phone = 13800138000\n\n-- 3. like以%开头\nWHERE name LIKE \'%Tom\'\n\n-- 4. OR连接非索引列\nWHERE name = \'Tom\' OR age = 18  -- age无索引则全表扫描\n\n-- 5. != 或 NOT IN（小表可能走索引，大表不走）\n```',
                                                                                                                                                                                        'MySQL索引基于B+树，叶子节点双向链表支持范围查询高效。联合索引遵循最左前缀原则，对索引列做函数/运算、隐式类型转换、like%前缀都会导致索引失效。',
                                                                                                                                                                                        1, 1, 2234, 198, 17, 1, 1, '2026-01-14 10:00:00', '2026-01-14 10:00:00'),

                                                                                                                                                                                       ('MySQL事务隔离级别与MVCC原理',
                                                                                                                                                                                        '# MySQL事务隔离级别与MVCC原理\n\n## 面试题目\n\n> MySQL有哪些隔离级别？InnoDB默认是什么？MVCC是如何实现的？\n\n## 四种隔离级别\n\n| 隔离级别 | 脏读 | 不可重复读 | 幻读 |\n|----------|------|-----------|------|\n| READ UNCOMMITTED | ✅可能 | ✅可能 | ✅可能 |\n| READ COMMITTED | ❌ | ✅可能 | ✅可能 |\n| **REPEATABLE READ**（默认） | ❌ | ❌ | ⚠️ 部分 |\n| SERIALIZABLE | ❌ | ❌ | ❌ |\n\n> InnoDB 在 RR 级别下通过 **Next-Key Lock（Gap Lock + Record Lock）** 解决大部分幻读。\n\n## MVCC（多版本并发控制）\n\n### 核心组件\n\n1. **隐藏列**：每行记录有 `trx_id`（最近修改的事务ID）和 `roll_pointer`（指向undo log的指针）\n\n2. **undo log**：记录数据的历史版本，形成版本链\n\n3. **Read View（一致性视图）**：记录当前活跃事务ID列表\n\n### Read View 可见性判断\n\n```\nRead View 包含：\n  m_ids: 当前活跃事务ID列表\n  min_trx_id: 活跃事务中最小的ID\n  max_trx_id: 下一个将分配的事务ID\n  creator_trx_id: 创建该Read View的事务ID\n\n判断规则：\n  trx_id == creator_trx_id  → 可见（自己修改的）\n  trx_id < min_trx_id       → 可见（已提交的旧事务）\n  trx_id >= max_trx_id      → 不可见（未来的事务）\n  trx_id in m_ids           → 不可见（活跃未提交）\n```\n\n### RC 与 RR 的区别\n\n- **READ COMMITTED**：每次 SELECT 都生成新的 Read View\n- **REPEATABLE READ**：事务开始时生成一次 Read View，整个事务复用\n\n这就是 RR 能实现可重复读的原因。',
                                                                                                                                                                                        'MySQL四种隔离级别，InnoDB默认RR。MVCC通过undo log版本链+Read View实现，RC每次查询生成新视图，RR事务开始只生成一次，保证可重复读。Next-Key Lock解决幻读。',
                                                                                                                                                                                        1, 1, 1876, 171, 14, 1, 0, '2026-01-15 14:00:00', '2026-01-15 14:00:00'),

                                                                                                                                                                                       ('MySQL EXPLAIN执行计划详解',
                                                                                                                                                                                        '# MySQL EXPLAIN执行计划详解\n\n## 面试题目\n\n> 如何分析SQL的执行计划？EXPLAIN各字段含义是什么？\n\n## EXPLAIN 输出字段\n\n```sql\nEXPLAIN SELECT * FROM user u\nJOIN order o ON u.id = o.user_id\nWHERE u.name = \'Tom\';\n```\n\n| 字段 | 说明 |\n|------|------|\n| id | 查询序号，id越大越先执行，id相同从上到下 |\n| **select_type** | 查询类型：SIMPLE/PRIMARY/SUBQUERY/UNION |\n| table | 当前行所访问的表 |\n| **type** | 访问类型（重点！） |\n| possible_keys | 可能用到的索引 |\n| **key** | 实际用到的索引 |\n| key_len | 索引使用的字节数 |\n| **rows** | 预估扫描行数 |\n| **Extra** | 附加信息（重点！） |\n\n## type 访问类型（从好到差）\n\n```\nsystem > const > eq_ref > ref > range > index > ALL\n```\n\n| type | 说明 |\n|------|------|\n| system | 表只有一行 |\n| const | 主键/唯一索引等值查询 |\n| eq_ref | join时被驱动表用主键/唯一索引 |\n| ref | 非唯一索引等值查询 |\n| range | 索引范围查询（between, >, <, in） |\n| index | 全索引扫描（比ALL好，但仍是全扫） |\n| ALL | **全表扫描，需要优化！** |\n\n> 生产环境要求：type 至少达到 **range**，最好是 **ref**\n\n## Extra 重要取值\n\n| Extra | 说明 |\n|-------|------|\n| Using index | 覆盖索引，无需回表 ✅ |\n| Using where | WHERE过滤，在存储层完成 |\n| Using filesort | 额外排序，无法用索引排序 ❌ |\n| Using temporary | 使用了临时表（GROUP BY/DISTINCT等）❌ |\n| Using index condition | 索引下推（ICP）优化 ✅ |\n\n## 优化案例\n\n```sql\n-- 慢查询：type=ALL, Extra=Using filesort\nSELECT * FROM order WHERE user_id = 1 ORDER BY create_time DESC;\n\n-- 优化：创建联合索引\nCREATE INDEX idx_userid_createtime ON order(user_id, create_time);\n-- 结果：type=ref, Extra=Using index condition\n```',
                                                                                                                                                                                        'EXPLAIN用于分析SQL执行计划，重点关注type（ALL需优化，至少达到range/ref）和Extra（Using filesort/temporary需优化，Using index覆盖索引最优）。',
                                                                                                                                                                                        1, 1, 1543, 134, 9, 1, 0, '2026-01-16 09:00:00', '2026-01-16 09:00:00'),

                                                                                                                                                                                       ('Redis五种数据结构及底层编码',
                                                                                                                                                                                        '# Redis五种数据结构及底层编码\n\n## 面试题目\n\n> Redis有哪些数据类型？它们的底层编码是什么？\n\n## 五种基本类型\n\n### String\n- **编码**：int（整数）、embstr（≤44字节）、raw（>44字节）\n- **用途**：缓存、计数器、分布式锁\n- **命令**：`SET/GET/INCR/DECR/SETNX/EXPIRE`\n\n### List\n- **编码**：ziplist（元素少且小）→ quicklist（JDK 3.2+默认）\n- **用途**：消息队列、最新列表、分页\n- **命令**：`LPUSH/RPUSH/LPOP/RPOP/LRANGE`\n\n### Hash\n- **编码**：ziplist（字段数≤128且值≤64字节）→ hashtable\n- **用途**：对象存储（用户信息）\n- **命令**：`HSET/HGET/HMSET/HGETALL`\n\n### Set\n- **编码**：intset（全整数且≤512）→ hashtable\n- **用途**：去重、标签、共同好友（SINTER）\n- **命令**：`SADD/SMEMBERS/SINTER/SUNION`\n\n### Sorted Set（ZSet）\n- **编码**：ziplist（元素数≤128且值≤64字节）→ **skiplist + hashtable**\n- **用途**：排行榜、带权重的队列\n- **命令**：`ZADD/ZRANGE/ZREVRANK/ZSCORE`\n\n## 跳表（SkipList）\n\nZSet 使用跳表实现，平均时间复杂度 **O(logN)**：\n\n```\nLevel 3: 1 ─────────────────── 9\nLevel 2: 1 ─────── 5 ───────── 9\nLevel 1: 1 ─── 3 ─ 5 ─── 7 ── 9\nLevel 0: 1 ─ 2 ─ 3 ─ 4 ─ 5 ─ 6 ─ 7 ─ 8 ─ 9\n```\n\n**为什么用跳表而不是红黑树？**\n1. 实现简单，代码量少\n2. 范围查询 `ZRANGE` 更高效（链表顺序遍历）\n3. 内存占用更灵活（随机层数）\n\n## Redis 3.2 新增数据类型\n\n| 类型 | 用途 |\n|------|------|\n| GEO | 地理位置（附近的人） |\n| HyperLogLog | 基数统计（UV统计，误差0.81%）|\n| Stream | 消息队列（持久化+Consumer Group） |',
                                                                                                                                                                                        'Redis五种基本类型String/List/Hash/Set/ZSet，底层编码根据数据大小自动切换（如ziplist→hashtable）。ZSet用跳表+hashtable实现O(logN)操作，范围查询高效。',
                                                                                                                                                                                        1, 1, 1678, 148, 11, 1, 0, '2026-01-17 10:00:00', '2026-01-17 10:00:00'),

                                                                                                                                                                                       ('Redis持久化：RDB与AOF对比',
                                                                                                                                                                                        '# Redis持久化：RDB与AOF对比\n\n## 面试题目\n\n> Redis的持久化方式有哪些？各有什么优缺点？生产环境如何选择？\n\n## RDB（Redis Database）\n\n**原理**：在指定时间间隔内将内存中的数据集快照写入磁盘（`.rdb`文件）。\n\n```redis\n# redis.conf 配置\nsave 900 1    # 900秒内至少1次修改则触发\nsave 300 10   # 300秒内至少10次修改则触发\nsave 60 10000 # 60秒内至少10000次修改则触发\n```\n\n**触发方式**：\n- `SAVE`：同步，阻塞主线程（不推荐）\n- `BGSAVE`：异步，**fork子进程**执行（利用Copy-On-Write）\n\n| 优点 | 缺点 |\n|------|------|\n| 文件紧凑，恢复速度快 | 可能丢失最后一次快照后的数据 |\n| 对性能影响小（COW） | fork时内存占用翻倍风险 |\n\n## AOF（Append Only File）\n\n**原理**：将每条写命令追加到 `.aof` 文件，恢复时重放所有命令。\n\n```redis\n# 三种同步策略\nappendfsync always    # 每次写命令都同步，最安全但最慢\nappendfsync everysec  # 每秒同步一次，推荐，最多丢1秒数据\nappendfsync no        # 由OS决定，性能最好但最不安全\n```\n\n**AOF重写**：`BGREWRITEAOF` 合并冗余命令，压缩文件体积。\n\n| 优点 | 缺点 |\n|------|------|\n| 数据安全性高（最多丢1秒）| 文件体积大，恢复慢 |\n| 可读性强，可手动修复 | 重写时有一定性能开销 |\n\n## 混合持久化（推荐）\n\nRedis 4.0+ 支持 AOF + RDB 混合模式：\n```redis\naof-use-rdb-preamble yes\n```\n\nAOF文件前半段是RDB数据（快速加载），后半段是增量AOF命令（数据完整）。\n\n## 生产选择建议\n\n| 场景 | 建议 |\n|------|------|\n| 纯缓存，允许丢失 | 关闭持久化 |\n| 数据重要，允许丢少量 | AOF + everysec |\n| 追求最佳方案 | 混合持久化 |',
                                                                                                                                                                                        'Redis持久化分RDB（定时快照，恢复快但可能丢数据）和AOF（记录写命令，安全但文件大）。生产推荐开启混合持久化（AOF+RDB），兼顾恢复速度和数据安全性。',
                                                                                                                                                                                        1, 1, 1345, 121, 8, 1, 0, '2026-01-18 14:00:00', '2026-01-18 14:00:00'),

                                                                                                                                                                                       ('缓存穿透、缓存击穿、缓存雪崩解决方案',
                                                                                                                                                                                        '# 缓存穿透、缓存击穿、缓存雪崩解决方案\n\n## 面试题目\n\n> 缓存三大问题是什么？如何解决？\n\n## 缓存穿透\n\n**定义**：查询一个**数据库中也不存在**的key，缓存永远不会命中，请求每次都打到DB。\n\n**攻击场景**：`GET /user/-1` 大量请求不存在的ID。\n\n**解决方案**：\n\n### 方案1：缓存空值\n```java\nUser user = db.findById(id);\nif (user == null) {\n    redis.set("user:" + id, "null", 5, TimeUnit.MINUTES); // 缓存空值，短暂过期\n}\n```\n\n### 方案2：布隆过滤器（推荐）\n```java\n// 启动时将所有有效ID加入布隆过滤器\nBloomFilter<Long> bloomFilter = BloomFilter.create(Funnels.longFunnel(), 1000000, 0.01);\nbloomFilter.put(validId);\n\n// 查询时先过滤\nif (!bloomFilter.mightContain(id)) {\n    return null; // 一定不存在，直接返回\n}\n```\n\n## 缓存击穿\n\n**定义**：一个**热点key过期**的瞬间，大量请求同时访问该key，全部穿透到DB。\n\n**解决方案**：\n\n### 方案1：互斥锁（强一致）\n```java\npublic User getUser(Long id) {\n    User user = redis.get("user:" + id);\n    if (user != null) return user;\n\n    // 获取分布式锁\n    String lockKey = "lock:user:" + id;\n    if (redis.setNX(lockKey, "1", 30, TimeUnit.SECONDS)) {\n        try {\n            user = db.findById(id);\n            redis.set("user:" + id, user, 30, TimeUnit.MINUTES);\n        } finally {\n            redis.del(lockKey);\n        }\n    } else {\n        Thread.sleep(50);\n        return getUser(id); // 重试\n    }\n    return user;\n}\n```\n\n### 方案2：逻辑过期（高可用）\n缓存永不过期，在value中存储过期时间，发现逻辑过期时异步更新。\n\n## 缓存雪崩\n\n**定义**：大量key在**同一时间集中过期**，或Redis宕机，导致所有请求打到DB。\n\n**解决方案**：\n\n```java\n// 1. 过期时间加随机值，避免集中过期\nredis.set(key, value, 30 + RandomUtil.randomInt(0, 10), TimeUnit.MINUTES);\n\n// 2. Redis高可用：主从 + 哨兵 / Redis Cluster\n// 3. 服务降级 + 熔断限流（Sentinel）\n// 4. 多级缓存（JVM本地缓存 + Redis）\n```',
                                                                                                                                                                                        '缓存穿透（查不存在的key）用布隆过滤器解决；缓存击穿（热点key过期）用互斥锁或逻辑过期解决；缓存雪崩（大量key同时过期）用随机过期时间+Redis高可用解决。',
                                                                                                                                                                                        1, 1, 2456, 213, 19, 1, 1, '2026-01-19 09:00:00', '2026-01-19 09:00:00');

CREATE TABLE `daily_stats` (
                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                               `stat_date` DATE NOT NULL UNIQUE,
                               `pv` BIGINT DEFAULT 0,
                               `uv` BIGINT DEFAULT 0,
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                               INDEX idx_stat_date (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `article_series` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `cover_image` VARCHAR(255) DEFAULT NULL,
  `sort_order` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_sort (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `article`
  ADD COLUMN `series_id` BIGINT DEFAULT NULL,
  ADD COLUMN `series_order` INT DEFAULT 0,
  ADD INDEX idx_series (`series_id`);

CREATE TABLE IF NOT EXISTS `user_reading_history` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `last_anchor` VARCHAR(255) DEFAULT '',
  `last_read_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_article (`user_id`, `article_id`),
  KEY idx_user_last_read (`user_id`, `last_read_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_subscription` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `muted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_target (`user_id`, `type`, `target_id`),
  KEY idx_type_target (`type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `type` VARCHAR(50) NOT NULL,
  `title` VARCHAR(200) NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `link` VARCHAR(255) DEFAULT '',
  `actor_id` BIGINT DEFAULT NULL,
  `actor_name` VARCHAR(100) DEFAULT '',
  `target_type` VARCHAR(50) DEFAULT '',
  `target_id` BIGINT DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_type_time (`type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `notification_id` BIGINT NOT NULL,
  `is_read` TINYINT DEFAULT 0,
  `read_at` DATETIME DEFAULT NULL,
  `is_deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user_unread (`user_id`, `is_read`, `is_deleted`, `create_time`),
  KEY idx_notification (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
