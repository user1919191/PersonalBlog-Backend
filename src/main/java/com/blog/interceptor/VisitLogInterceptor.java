package main.java.com.blog.interceptor;

import com.blog.service.VisitLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class VisitLogInterceptor implements HandlerInterceptor {

    private final VisitLogService visitLogService;

    public VisitLogInterceptor(VisitLogService visitLogService) {
        this.visitLogService = visitLogService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;

        if (path.startsWith("/admin") || path.startsWith("/uploads")) {
            return;
        }

        if (!isCountableUri(path)) {
            return;
        }

        int status = response.getStatus();
        if (status < 200 || status >= 400) {
            return;
        }

        visitLogService.recordVisit(buildVisitorId(request));
    }

    private boolean isCountableUri(String uri) {
        return uri.startsWith("/articles")
                || uri.startsWith("/interviews")
                || uri.startsWith("/featured")
                || uri.startsWith("/navs");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String buildVisitorId(HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String normalizedUa = userAgent == null ? "unknown" : userAgent;
        return (ip == null ? "unknown" : ip) + "::" + normalizedUa;
    }
}