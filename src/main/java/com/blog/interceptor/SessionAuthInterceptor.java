package main.java.com.blog.interceptor;

import com.blog.dto.SessionUser;
import com.blog.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class SessionAuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;

    public SessionAuthInterceptor(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS 预检请求不携带 Authorization，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 公开可读的 GET 路径，无需认证
        if ("GET".equalsIgnoreCase(method) &&
            (path.contains("/comments/article/") ||
             path.contains("/interview-comments/interview/") ||
             path.contains("/forum/"))) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            write401(response, "未登录或登录已过期");
            return false;
        }

        String sessionId = header.substring(7);
        // Item 3：传入当前请求 IP，用于异常检测和记录
        String currentIp = getClientIp(request);
        Optional<SessionUser> sessionUser = sessionService.getSession(sessionId, currentIp);
        if (sessionUser.isEmpty()) {
            write401(response, "登录已过期，请重新登录");
            return false;
        }

        request.setAttribute("userId", sessionUser.get().getUserId());
        request.setAttribute("role", sessionUser.get().getRole());
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void write401(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
