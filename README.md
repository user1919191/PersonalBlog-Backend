# 个人博客系统

一个基于 **Spring Boot 3 + Vue 3** 的前后端分离博客系统，包含博客文章、面经题库、精选文章、学习导航、留言论坛、收藏、订阅与站内通知，以及完整的管理后台。

## 功能特性

### 前台

- **首页 / 博客列表**：分类、标签、关键字筛选，分页浏览
- **文章详情**：Markdown 渲染、代码高亮、目录导航、阅读进度记忆、点赞、评论与回复
- **面经题库**：按分类浏览面经，支持点赞与评论
- **精选文章**：按分类查看外部优质文章
- **学习网址**：常用开发工具、文档、社区导航
- **留言论坛**：发帖、回复、置顶、删除（作者或管理员）
- **收藏夹**：创建/重命名/删除收藏夹，收藏文章、精选、面经，支持掌握状态
- **个人中心**：修改显示名、管理收藏夹
- **订阅与通知**：订阅作者/标签，文章更新通知，站内通知已读
- **实用工具**：JSON 格式化、URL 编解码、时间转换、正则测试、JVM/Git/MySQL 指令速查

### 后台

- 数据仪表盘（访问趋势、热门文章、内容与用户统计）
- 分类、专栏、标签管理
- 文章管理（Markdown 编辑、封面上传、草稿/发布、置顶、可见性、专栏关联）
- 面经管理
- 精选文章、学习网址管理
- 论坛帖子与回复管理（置顶、软删除/恢复）
- 用户管理（封禁/解封、踢下线、在线 Session 统计）

> 日程待办、系统监控、API 巡检功能已移除，前后端不再包含相关代码与接口。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Java 17+、Spring Boot 3.2、MyBatis-Plus、MySQL 8、Redis、JWT、Maven |
| 前端 | Vue 3、Vite 5、Vue Router 4、Pinia、Element Plus、Axios、md-editor-v3、ECharts |
| 存储 | MySQL（业务数据）、Redis（用户 Session、缓存、限流、点赞） |
| 集成 | 腾讯云 COS（图片上传）、Gitee / GitHub OAuth 登录 |

## 目录结构

```text
Blog/
├── blog-backend/                 # Spring Boot 后端
│   ├── src/main/java/com/blog/   # controller / service / repository / entity ...
│   ├── src/main/resources/       # application.yml、application-prod.yml、db 脚本
│   └── pom.xml
├── blog_system.sql               # 完整数据库初始化脚本（22 张表）
├── .env.prod.example             # 后端环境变量示例
└── README.md
```

## 环境要求

- JDK 17 及以上（已在 JDK 22 验证）
- MySQL 8.x
- Redis 6.x 及以上
- Node.js 18 及以上、npm
- 可选：腾讯云 COS、Gitee/GitHub OAuth 应用

## 快速开始

### 1. 初始化数据库

使用根目录的整合脚本创建数据库、表结构和默认管理员：

```bash
mysql -uroot -p < blog_system.sql
```

脚本会：

- 创建数据库 `blog_system`（utf8mb4）
- 创建 22 张业务表及索引
- 写入默认管理员账号与 3 个默认分类

默认管理员：

```text
用户名：admin
密码：admin123
```

> 首次登录后请立即修改密码。生产环境请通过 `BLOG_ADMIN_PASSWORD` 指定初始密码。

### 2. 配置后端环境变量

复制示例文件后填写真实值（`.env` 不要提交到 Git）：

```bash
cp .env.prod.example .env
```

主要变量：

| 变量 | 说明 |
| --- | --- |
| `BLOG_DB_URL` | JDBC 连接串，例如 `jdbc:mysql://127.0.0.1:3306/blog_system?...` |
| `BLOG_DB_USERNAME` / `BLOG_DB_PASSWORD` | 数据库账号密码 |
| `BLOG_REDIS_HOST` / `BLOG_REDIS_PORT` / `BLOG_REDIS_PASSWORD` / `BLOG_REDIS_DATABASE` | Redis 配置 |
| `BLOG_JWT_SECRET` | 管理员 JWT 密钥，至少 32 位随机字符串 |
| `BLOG_SITE_URL` | 站点访问地址，用于分享链接 |
| `BLOG_UPLOAD_PATH` | 本地图片上传目录 |
| `BLOG_ADMIN_USERNAME` / `BLOG_ADMIN_PASSWORD` | 管理员初始账号/密码 |
| `BLOG_COS_SECRET_ID` / `BLOG_COS_SECRET_KEY` / `BLOG_COS_REGION` / `BLOG_COS_BUCKET_NAME` / `BLOG_COS_URL_PREFIX` | 腾讯云 COS 配置（可选） |
| `BLOG_GITEE_CLIENT_ID` / `BLOG_GITEE_CLIENT_SECRET` / `BLOG_GITEE_REDIRECT_URI` | Gitee OAuth（可选） |
| `BLOG_GITHUB_CLIENT_ID` / `BLOG_GITHUB_CLIENT_SECRET` / `BLOG_GITHUB_REDIRECT_URI` | GitHub OAuth（可选） |

本地 PowerShell 启动示例：

```powershell
$env:BLOG_DB_PASSWORD = "你的数据库密码"
$env:BLOG_REDIS_PASSWORD = ""
$env:BLOG_JWT_SECRET = "至少32位随机字符串"
$env:BLOG_ADMIN_PASSWORD = "admin123"

cd blog-backend
..\mvnw.cmd -DskipTests package
java -jar target\blog-backend-1.0.0.jar
```

后端地址：`http://127.0.0.1:8080/api`

### 3. 启动前端

```bash
cd blog-frontend
npm install
npm run dev
```

前端地址：`http://127.0.0.1:5173`

开发环境下前端通过 `/api` 请求后端，由 Vite 代理到 `http://127.0.0.1:8080`，无需额外配置跨域。

### 4. 登录后台

- 地址：`http://127.0.0.1:5173/admin/login`
- 账号：`admin`
- 密码：`admin123`（或 `BLOG_ADMIN_PASSWORD` 设置的值）

## 接口概览

| 模块 | 路径前缀 | 鉴权 |
| --- | --- | --- |
| 认证 | `/api/auth/**` | 公开 |
| 文章 / 分类 / 标签 | `/api/articles`、`/api/categories`、`/api/articles/tags` | 公开 |
| 精选文章 | `/api/featured`、`/api/featured/categories` | 公开 |
| 面经 | `/api/interviews` | 公开 |
| 学习网址 | `/api/navs` | 公开 |
| 论坛 | `/api/forum/posts`、`/api/forum/replies` | 浏览公开，发布/删除需登录 |
| 文章评论 | `/api/comments/article/{id}` | 浏览公开，发布需登录 |
| 面经评论 | `/api/interview-comments/interview/{id}` | 浏览公开，发布需登录 |
| 用户中心 | `/api/user/**` | 登录（Bearer Session） |
| 管理后台 | `/api/admin/**`、`/api/series/admin/**` | 管理员（Bearer JWT） |

鉴权说明：

- 管理员：登录后返回 JWT，前端存于 `admin_token`
- 普通用户：Gitee/GitHub OAuth 登录后返回 Redis Session ID，前端存于 `user_token`
- 点赞按客户端 IP 记录在 Redis，无需登录也可点赞

## 数据库表

整合脚本包含 22 张表：

- 内容：`article`、`article_series`、`article_tag`、`tag`、`category`、`comment`、`article_like`
- 面经：`interview`、`interview_comment`
- 精选 / 导航：`featured_article`、`website_nav`
- 论坛：`forum_post`、`forum_reply`
- 用户与社交：`user`、`favorite_folder`、`favorite_item`、`user_reading_history`、`user_subscription`
- 通知：`notification`、`user_notification`
- 统计：`visit_log`、`daily_stats`

## 生产构建

后端：

```bash
cd blog-backend
..\mvnw.cmd -DskipTests package
# 产物：target/blog-backend-1.0.0.jar
```

前端：

```bash
cd blog-frontend
npm run build
# 产物：dist/
```

生产部署使用 `application-prod.yml`，所有敏感配置均由环境变量注入；Nginx 需将 `/api` 反向代理到后端端口，`/uploads` 映射上传目录。

## 安全说明

- 仓库中的配置文件均为环境变量占位示例，不包含真实密码或密钥
- `.env`、`application-local.yml`、日志、上传目录、构建产物已加入 `.gitignore`
- 生产环境请务必修改默认管理员密码，并使用随机 JWT 密钥
- 若曾将真实密钥写入文件或提交过历史记录，请到腾讯云、Gitee、GitHub、MySQL、Redis 控制台重置对应凭据

## 常见问题

| 问题 | 排查方向 |
| --- | --- |
| 后端启动报数据库连接失败 | 检查 `BLOG_DB_*`，确认 `blog_system` 已初始化 |
| 报 Redis 连接失败 | 确认 Redis 已启动，检查 `BLOG_REDIS_*` |
| 后台登录 401 或跳回登录页 | 清除 `localStorage` 中的 `admin_token` 后重新登录 |
| 前端请求 Network Error | 确认 Vite 已启动且代理目标为 `127.0.0.1:8080` |
| OAuth 登录失败 | 检查回调地址与 Gitee/GitHub 应用配置是否一致 |
| 图片上传失败 | 检查 COS 配置或本地上传目录权限 |
