package main.java.com.blog.controller;

import com.blog.service.SitemapService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SitemapController {

    private final SitemapService sitemapService;

    @Value("${blog.site.url}")
    private String siteUrl;

    public SitemapController(SitemapService sitemapService) {
        this.sitemapService = sitemapService;
    }

    /** 返回 sitemap.xml，供搜索引擎爬取 */
    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> sitemap() {
        String xml = sitemapService.getXml();
        if (xml == null || xml.isBlank()) {
            return ResponseEntity.internalServerError().body("<!-- sitemap not ready -->");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/xml;charset=UTF-8"))
                .body(xml);
    }

    /** 返回 robots.txt，指引爬虫行为 */
    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> robots() {
        String body = "User-agent: *\n"
                + "Allow: /\n"
                + "Disallow: /admin/\n"
                + "\n"
                + "Sitemap: " + siteUrl + "/sitemap.xml\n";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
                .body(body);
    }
}
