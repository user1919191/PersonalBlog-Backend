package main.java.com.blog.service;

import com.blog.dto.LoginResponse;
import com.blog.entity.User;
import com.blog.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GitHubOAuthService {

    @Value("${github.oauth.client-id}")
    private String clientId;

    @Value("${github.oauth.client-secret}")
    private String clientSecret;

    @Value("${github.oauth.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubOAuthService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    /** 返回前端跳转的 GitHub 授权地址（state=github 用于回调时识别来源） */
    public String getAuthorizationUrl() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=user:email" +
                "&state=github";
    }

    /** 用 code 换 token，再获取用户信息，最终返回 Session ID */
    public LoginResponse callback(String code, String ip, String userAgent) {
        String accessToken = fetchAccessToken(code);
        JsonNode userInfo = fetchUserInfo(accessToken);

        String githubId = userInfo.get("id").asText();
        String login = userInfo.get("login").asText();
        String name = userInfo.has("name") && !userInfo.get("name").isNull()
                ? userInfo.get("name").asText() : login;
        String avatarUrl = userInfo.has("avatar_url") ? userInfo.get("avatar_url").asText() : null;

        // 查库或自动注册
        User user = userRepository.findByGithubId(githubId).orElseGet(() -> {
            User newUser = new User();
            newUser.setGithubId(githubId);
            newUser.setOauthProvider("github");
            newUser.setUsername(resolveUsername(login));
            newUser.setDisplayName(name);
            newUser.setAvatar(avatarUrl);
            newUser.setRole("USER");
            newUser.setStatus(1);
            userRepository.insert(newUser);
            return newUser;
        });

        // 更新头像（每次登录同步）
        if (avatarUrl != null && !avatarUrl.equals(user.getAvatar())) {
            user.setAvatar(avatarUrl);
            userRepository.updateById(user);
        }

        // 检查账户是否被封禁
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账户已被封禁，请联系管理员");
        }

        String sessionId = sessionService.createSession(user, ip, userAgent);
        return new LoginResponse(sessionId, user.getId(), user.getUsername(), user.getAvatar(), user.getRole());
    }

    private String fetchAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://github.com/login/oauth/access_token",
                new HttpEntity<>(params, headers),
                String.class
        );

        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            if (!node.has("access_token")) {
                throw new IllegalArgumentException("GitHub 授权失败：" + response.getBody());
            }
            return node.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("解析 GitHub token 失败", e);
        }
    }

    private JsonNode fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github+json");
        headers.set("User-Agent", "Blog-App");

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("解析 GitHub 用户信息失败", e);
        }
    }

    /** 用户名冲突时自动加后缀 */
    private String resolveUsername(String base) {
        String candidate = "github_" + base;
        int suffix = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = "github_" + base + "_" + suffix++;
        }
        return candidate;
    }
}
