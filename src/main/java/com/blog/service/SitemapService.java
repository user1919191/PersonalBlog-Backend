package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Article;
import com.blog.entity.Interview;
import com.blog.repository.ArticleRepository;
import com.blog.repository.InterviewRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Service
public class SitemapService {

    private static final Logger log = LoggerFactory.getLogger(SitemapService.class);
    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    @Value("${blog.site.url}")
    private String siteUrl;

    private final ArticleRepository articleRepository;
    private final InterviewRepository interviewRepository;

    private volatile String cachedXml = "";

    public SitemapService(ArticleRepository articleRepository, InterviewRepository interviewRepository) {
        this.articleRepository = articleRepository;
        this.interviewRepository = interviewRepository;
    }

    public String getXml() {
        return cachedXml;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduledRefresh() {
        refresh();
    }

    @Async
    public void triggerRefresh() {
        refresh();
    }

    public void refresh() {
        try {
            cachedXml = buildXml();
            log.info("[Sitemap] regenerated successfully");
            pingSearchEngines();
        } catch (Exception e) {
            log.warn("[Sitemap] refresh failed: {}", e.getMessage());
        }
    }

    private String buildXml() {
        List<Article> articles = articleRepository.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
                        .select(Article::getId, Article::getUpdateTime)
        );

        List<Interview> interviews = interviewRepository.selectList(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getStatus, 1)
                        .eq(Interview::getVisibility, VISIBILITY_PUBLIC)
                        .select(Interview::getId, Interview::getUpdateTime)
        );

        String today = LocalDate.now().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        appendUrl(sb, siteUrl + "/", today, "weekly", "1.0");
        appendUrl(sb, siteUrl + "/blog", today, "daily", "0.9");
        appendUrl(sb, siteUrl + "/interviews", today, "daily", "0.8");
        appendUrl(sb, siteUrl + "/featured", today, "weekly", "0.6");
        appendUrl(sb, siteUrl + "/navs", today, "monthly", "0.5");

        for (Article article : articles) {
            String lastmod = article.getUpdateTime() != null
                    ? article.getUpdateTime().toLocalDate().toString()
                    : today;
            appendUrl(sb, siteUrl + "/article/" + article.getId(), lastmod, "monthly", "0.8");
        }

        for (Interview interview : interviews) {
            String lastmod = interview.getUpdateTime() != null
                    ? interview.getUpdateTime().toLocalDate().toString()
                    : today;
            appendUrl(sb, siteUrl + "/interview/" + interview.getId(), lastmod, "monthly", "0.7");
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    private void appendUrl(StringBuilder sb, String loc, String lastmod,
                           String changefreq, String priority) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(escapeXml(loc)).append("</loc>\n");
        sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        sb.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sb.append("    <priority>").append(priority).append("</priority>\n");
        sb.append("  </url>\n");
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    @Async
    public void pingSearchEngines() {
        if (siteUrl.contains("localhost")) {
            log.info("[Sitemap] skip ping (localhost)");
            return;
        }

        String encodedUrl = URLEncoder.encode(siteUrl + "/sitemap.xml", StandardCharsets.UTF_8);

        pingGet("https://www.google.com/ping?sitemap=" + encodedUrl, "Google");
        pingGet("https://www.bing.com/ping?sitemap=" + encodedUrl, "Bing");
        log.info("[Sitemap] 百度 Sitemap 请在 https://ziyuan.baidu.com/linksubmit/index 手动提交: {}/sitemap.xml", siteUrl);
    }

    private void pingGet(String url, String engine) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            log.info("[Sitemap] pinged {} -> HTTP {}", engine, resp.statusCode());
        } catch (Exception e) {
            log.warn("[Sitemap] ping {} failed: {}", engine, e.getMessage());
        }
    }
}