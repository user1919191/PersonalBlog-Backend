package main.java.com.blog.interceptor;

import com.blog.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS 预检请求不携带 Authorization，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            write401(response, "未登录或登录已过期");
            return false;
        }

        String token = header.substring(7);
        if (!jwtUtil.isValid(token)) {
            write401(response, "Token无效或已过期");
            return false;
        }

        String role = jwtUtil.getRole(token);
        Long userId = jwtUtil.getUserId(token);

        // /admin/** 只允许 ADMIN 角色
        if (path.contains("/admin/") && !"ADMIN".equals(role)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"无管理员权限\",\"data\":null}");
            return false;
        }

        request.setAttribute("userId", userId);
        request.setAttribute("role", role);
        return true;
    }

    private void write401(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
