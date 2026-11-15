package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleSeriesDTO;
import com.blog.dto.PageResult;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.ArticleTagRepository;
import com.blog.repository.TagRepository;
import com.blog.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String VISIBILITY_PRIVATE = "PRIVATE";

    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SitemapService sitemapService;
    private final ArticleSeriesService articleSeriesService;
    private final NotificationService notificationService;
    private final VisitLogService visitLogService;

    public ArticleService(ArticleRepository articleRepository,
                          ArticleTagRepository articleTagRepository,
                          TagRepository tagRepository,
                          UserRepository userRepository,
                          RedisTemplate<String, Object> redisTemplate,
                          SitemapService sitemapService,
                          @Lazy ArticleSeriesService articleSeriesService,
                          NotificationService notificationService,
                          VisitLogService visitLogService) {
        this.articleRepository = articleRepository;
        this.articleTagRepository = articleTagRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.sitemapService = sitemapService;
        this.articleSeriesService = articleSeriesService;
        this.notificationService = notificationService;
        this.visitLogService = visitLogService;
    }

    public PageResult<Article> getArticles(Integer pageNum, Integer pageSize, Long categoryId, Long tagId, String keyword) {
        List<Long> tagArticleIds = null;
        if (tagId != null) {
            tagArticleIds = articleTagRepository.selectList(
                    new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId)
            ).stream().map(ArticleTag::getArticleId).collect(Collectors.toList());
            if (tagArticleIds.isEmpty()) {
                return new PageResult<>(0L, pageNum, pageSize, Collections.emptyList());
            }
        }

        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getStatus, 1)
                .eq(Article::getVisibility, VISIBILITY_PUBLIC);
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (tagArticleIds != null) {
            wrapper.in(Article::getId, tagArticleIds);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getContent, keyword));
        }
        wrapper.orderByDesc(Article::getIsTop, Article::getCreateTime);

        Page<Article> result = articleRepository.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords());
    }

    public PageResult<Article> adminGetArticles(Integer pageNum, Integer pageSize, Long categoryId, String keyword) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(Article::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Article::getTitle, keyword).or().like(Article::getSummary, keyword));
        }
        wrapper.orderByDesc(Article::getCreateTime);

        Page<Article> result = articleRepository.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords());
    }

    public Article getArticleById(Long id) {
        String cacheKey = "article:" + id;
        Article article = (Article) redisTemplate.opsForValue().get(cacheKey);

        if (article == null) {
            article = articleRepository.selectById(id);
            assertPublicArticle(article);
            redisTemplate.opsForValue().set(cacheKey, article, 1, TimeUnit.HOURS);
        } else {
            assertPublicArticle(article);
        }

        fillArticleExtra(article);
        incrementViewCount(id);
        visitLogService.recordArticleVisit(id);
        return article;
    }

    public Article getAdminArticleById(Long id) {
        Article article = articleRepository.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        fillArticleExtra(article);
        return article;
    }

    public Article getArticleForOg(Long id) {
        Article article = articleRepository.selectById(id);
        assertPublicArticle(article);
        return article;
    }

    @Transactional
    public Article createArticle(ArticleRequest request) {
        Article article = new Article();
        applyRequest(article, request);
        article.setAuthorId(1L);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        articleRepository.insert(article);
        saveArticleTags(article.getId(), request.getTagIds());
        redisTemplate.delete("article:" + article.getId());
        sitemapService.triggerRefresh();
        if (isPublishedPublic(article)) {
            notificationService.notifyArticlePublished(article, safeTagIds(request.getTagIds()));
        }
        fillArticleExtra(article);
        return article;
    }

    @Transactional
    public Article updateArticle(Long id, ArticleRequest request) {
        Article article = articleRepository.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }

        boolean wasPublishedPublic = isPublishedPublic(article);
        applyRequest(article, request);
        articleRepository.updateById(article);
        saveArticleTags(id, request.getTagIds());
        redisTemplate.delete("article:" + id);
        sitemapService.triggerRefresh();
        if (isPublishedPublic(article)) {
            if (wasPublishedPublic) {
                notificationService.notifyArticleUpdated(article, safeTagIds(request.getTagIds()));
            } else {
                notificationService.notifyArticlePublished(article, safeTagIds(request.getTagIds()));
            }
        }
        fillArticleExtra(article);
        return article;
    }

    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        articleRepository.deleteById(id);
        articleTagRepository.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, id));
        redisTemplate.delete("article:" + id);
        sitemapService.triggerRefresh();
    }

    @Transactional
    public void likeArticle(Long articleId, String clientId, Long actorUserId, String actorName) {
        String likeKey = "article:like:" + articleId + ":" + clientId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(likeKey))) {
            throw new IllegalArgumentException("请勿重复点赞");
        }
        Article article = requirePublicArticle(articleId);
        redisTemplate.opsForValue().set(likeKey, "1", 7, TimeUnit.DAYS);
        article.setLikeCount(article.getLikeCount() + 1);
        articleRepository.updateById(article);
        redisTemplate.delete("article:" + articleId);
        notificationService.notifyArticleLiked(articleId, actorUserId, actorName);
    }

    @Transactional
    public void unlikeArticle(Long articleId, String clientId) {
        String likeKey = "article:like:" + articleId + ":" + clientId;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(likeKey))) {
            throw new IllegalArgumentException("尚未点赞");
        }
        redisTemplate.delete(likeKey);
        Article article = requirePublicArticle(articleId);
        article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
        articleRepository.updateById(article);
        redisTemplate.delete("article:" + articleId);
    }

    public List<Tag> getAllTags() {
        return tagRepository.selectList(
                new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getArticleCount).orderByAsc(Tag::getCreateTime)
        );
    }

    private void applyRequest(Article article, ArticleRequest request) {
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        article.setStatus(request.getStatus());
        article.setVisibility(normalizeVisibility(request.getVisibility()));
        article.setIsTop(request.getIsTop());
        article.setSeriesId(request.getSeriesId());
        article.setSeriesOrder(request.getSeriesId() != null
                ? (request.getSeriesOrder() == null ? 1 : request.getSeriesOrder())
                : null);
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        List<Long> oldTagIds = articleTagRepository.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId)
        ).stream().map(ArticleTag::getTagId).collect(Collectors.toList());

        articleTagRepository.delete(new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId));
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                ArticleTag relation = new ArticleTag();
                relation.setArticleId(articleId);
                relation.setTagId(tagId);
                articleTagRepository.insert(relation);
            }
        }

        List<Long> refreshIds = new ArrayList<>(oldTagIds);
        if (tagIds != null) {
            refreshIds.addAll(tagIds);
        }
        refreshTagCounts(refreshIds.stream().distinct().collect(Collectors.toList()));
    }

    private void refreshTagCounts(List<Long> tagIds) {
        for (Long tagId : tagIds) {
            long count = articleTagRepository.selectCount(
                    new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getTagId, tagId)
            );
            Tag tag = tagRepository.selectById(tagId);
            if (tag != null) {
                tag.setArticleCount((int) count);
                tagRepository.updateById(tag);
            }
        }
    }

    private List<Tag> getTagsByArticleId(Long articleId) {
        List<Long> tagIds = articleTagRepository.selectList(
                new LambdaQueryWrapper<ArticleTag>().eq(ArticleTag::getArticleId, articleId)
        ).stream().map(ArticleTag::getTagId).collect(Collectors.toList());
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return tagRepository.selectBatchIds(tagIds);
    }

    private void fillArticleExtra(Article article) {
        article.setTags(getTagsByArticleId(article.getId()));
        User author = article.getAuthorId() != null ? userRepository.selectById(article.getAuthorId()) : null;
        if (author != null) {
            article.setAuthorName(author.getDisplayName() != null && !author.getDisplayName().isBlank()
                    ? author.getDisplayName()
                    : author.getUsername());
        }
        if (article.getSeriesId() != null) {
            try {
                ArticleSeriesDTO series = articleSeriesService.getSeriesById(article.getSeriesId());
                article.setSeries(series);
                Integer currentOrder = article.getSeriesOrder() == null ? 1 : article.getSeriesOrder();
                article.setSeriesNav(articleSeriesService.getSeriesNavigation(article.getId(), article.getSeriesId(), currentOrder));
            } catch (Exception ignored) {
            }
        }
    }

    private List<Long> safeTagIds(List<Long> tagIds) {
        return tagIds != null ? tagIds : Collections.emptyList();
    }

    private void incrementViewCount(Long articleId) {
        String key = "article:view:" + articleId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count % 100 == 0) {
            Article article = articleRepository.selectById(articleId);
            if (article != null) {
                article.setViewCount(article.getViewCount() + 100);
                articleRepository.updateById(article);
            }
        }
    }

    private Article requirePublicArticle(Long articleId) {
        Article article = articleRepository.selectById(articleId);
        assertPublicArticle(article);
        return article;
    }

    private void assertPublicArticle(Article article) {
        if (!isPublishedPublic(article)) {
            throw new IllegalArgumentException("文章不存在");
        }
    }

    private boolean isPublishedPublic(Article article) {
        return article != null
                && article.getStatus() != null
                && article.getStatus() == 1
                && VISIBILITY_PUBLIC.equals(article.getVisibility());
    }

    private String normalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return VISIBILITY_PUBLIC;
        }
        String normalized = visibility.trim().toUpperCase(Locale.ROOT);
        if (VISIBILITY_PUBLIC.equals(normalized) || VISIBILITY_PRIVATE.equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("无效的可见性设置");
    }
}