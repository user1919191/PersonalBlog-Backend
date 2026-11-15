package main.java.com.blog.controller;

import com.blog.dto.LoginRequest;
import com.blog.dto.LoginResponse;
import com.blog.dto.Result;
import com.blog.service.AuthService;
import com.blog.service.GiteeOAuthService;
import com.blog.service.GitHubOAuthService;
import com.blog.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final GiteeOAuthService giteeOAuthService;
    private final GitHubOAuthService gitHubOAuthService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, GiteeOAuthService giteeOAuthService,
                          GitHubOAuthService gitHubOAuthService, SessionService sessionService) {
        this.authService = authService;
        this.giteeOAuthService = giteeOAuthService;
        this.gitHubOAuthService = gitHubOAuthService;
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    /** 前端调此接口获取 Gitee 授权跳转地址 */
    @GetMapping("/gitee/url")
    public Result<String> giteeUrl() {
        return Result.success(giteeOAuthService.getAuthorizationUrl());
    }

    /** Gitee 回调：前端把 code 传过来，返回 Session ID（Item 3：提取 IP/UA） */
    @PostMapping("/gitee/callback")
    public Result<LoginResponse> giteeCallback(@RequestParam String code, HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        return Result.success("登录成功", giteeOAuthService.callback(code, ip, ua));
    }

    /** 用户注销：删除 Redis 中的 Session */
    @PostMapping("/logout/user")
    public Result<Void> logoutUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            sessionService.deleteSession(header.substring(7));
        }
        return Result.success("已退出登录", null);
    }

    /** Item 2（管理员）：强制踢出某用户所有设备 */
    @DeleteMapping("/admin/users/{userId}/sessions")
    public Result<Void> revokeUserSessions(@PathVariable Long userId) {
        sessionService.invalidateAllSessions(userId);
        return Result.success("已清除用户所有 Session", null);
    }

    /** 前端调此接口获取 GitHub 授权跳转地址 */
    @GetMapping("/github/url")
    public Result<String> githubUrl() {
        return Result.success(gitHubOAuthService.getAuthorizationUrl());
    }

    /** GitHub 回调：前端把 code 传过来，返回 Session ID */
    @PostMapping("/github/callback")
    public Result<LoginResponse> githubCallback(@RequestParam String code, HttpServletRequest request) {
        String ip = getClientIp(request);
        String ua = request.getHeader("User-Agent");
        return Result.success("登录成功", gitHubOAuthService.callback(code, ip, ua));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
