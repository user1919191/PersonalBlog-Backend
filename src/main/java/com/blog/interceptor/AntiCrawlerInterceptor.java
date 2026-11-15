package main.java.com.blog.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

public class AntiCrawlerInterceptor implements HandlerInterceptor {

    private static final List<String> BOT_UA_KEYWORDS = Arrays.asList(
            "bot", "crawler", "spider", "scraper", "python-requests",
            "scrapy", "curl", "wget", "java/", "go-http-client",
            "libwww-perl", "jakarta", "mechanize"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isBlank()) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"Forbidden\",\"data\":null}");
            return false;
        }
        String uaLower = ua.toLowerCase();
        for (String keyword : BOT_UA_KEYWORDS) {
            if (uaLower.contains(keyword)) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"Forbidden\",\"data\":null}");
                return false;
            }
        }
        return true;
    }
}
