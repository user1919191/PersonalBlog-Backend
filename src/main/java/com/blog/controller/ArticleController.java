package main.java.com.blog.controller;

import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.Article;
import com.blog.entity.Tag;
import com.blog.service.ArticleService;
import com.blog.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final SessionService sessionService;

    @Value("${blog.site.url}")
    private String siteUrl;

    @Value("${cos.url-prefix}")
    private String cosUrlPrefix;

    public ArticleController(ArticleService articleService, SessionService sessionService) {
        this.articleService = articleService;
        this.sessionService = sessionService;
    }

    @GetMapping
    public Result<PageResult<Article>> getArticles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.getArticles(pageNum, pageSize, categoryId, tagId, keyword));
    }

    @GetMapping("/tags")
    public Result<List<Tag>> getTags() {
        return Result.success(articleService.getAllTags());
    }

    @GetMapping("/{id}")
    public Result<Article> getArticleById(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    @GetMapping(value = "/{id}/og", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getArticleOg(@PathVariable Long id) {
        try {
            Article article = articleService.getArticleForOg(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(buildOgHtml(article));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeArticle(@PathVariable Long id, HttpServletRequest request) {
        String clientId = getClientId(request);
        Long actorUserId = null;
        String actorName = null;

        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String sessionId = authorization.substring(7);
            var sessionUser = sessionService.getSession(sessionId, clientId);
            if (sessionUser.isPresent()) {
                actorUserId = sessionUser.get().getUserId();
                actorName = sessionUser.get().getDisplayName();
            }
        }

        articleService.likeArticle(id, clientId, actorUserId, actorName);
        return Result.success("点赞成功", null);
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeArticle(@PathVariable Long id, HttpServletRequest request) {
        articleService.unlikeArticle(id, getClientId(request));
        return Result.success("取消点赞成功", null);
    }

    private String getClientId(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String buildOgHtml(Article article) {
        String title = escapeHtml(article.getTitle());
        String rawDesc = (article.getSummary() != null && !article.getSummary().isBlank())
                ? article.getSummary()
                : article.getContent()
                        .replaceAll("[#*`>\\[\\]!|\\-]", " ")
                        .replaceAll("\\s+", " ")
                        .trim();
        String desc = escapeHtml(rawDesc.substring(0, Math.min(150, rawDesc.length())));
        String image = (article.getCoverImage() != null && !article.getCoverImage().isBlank())
                ? article.getCoverImage()
                : cosUrlPrefix + "/default-og.png";
        String url = siteUrl + "/article/" + article.getId();

        return "<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<title>" + title + "</title>\n"
                + "<meta property=\"og:type\" content=\"article\">\n"
                + "<meta property=\"og:title\" content=\"" + title + "\">\n"
                + "<meta property=\"og:description\" content=\"" + desc + "\">\n"
                + "<meta property=\"og:image\" content=\"" + image + "\">\n"
                + "<meta property=\"og:url\" content=\"" + url + "\">\n"
                + "<meta property=\"og:site_name\" content=\"Su的博客\">\n"
                + "<meta name=\"twitter:card\" content=\"summary_large_image\">\n"
                + "<meta name=\"twitter:title\" content=\"" + title + "\">\n"
                + "<meta name=\"twitter:description\" content=\"" + desc + "\">\n"
                + "<meta name=\"twitter:image\" content=\"" + image + "\">\n"
                + "<meta http-equiv=\"refresh\" content=\"0;url=" + url + "\">\n"
                + "</head>\n<body>\n"
                + "<a href=\"" + url + "\">" + title + "</a>\n"
                + "</body>\n</html>";
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
