package main.java.com.blog.service;

import com.blog.dto.LoginResponse;
import com.blog.entity.User;
import com.blog.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GiteeOAuthService {

    @Value("${gitee.oauth.client-id}")
    private String clientId;

    @Value("${gitee.oauth.client-secret}")
    private String clientSecret;

    @Value("${gitee.oauth.redirect-uri}")
    private String redirectUri;

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GiteeOAuthService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public String getAuthorizationUrl() {
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return "https://gitee.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&redirect_uri=" + encodedRedirectUri +
                "&response_type=code" +
                "&scope=user_info";
    }

    public LoginResponse callback(String code, String ip, String userAgent) {
        String accessToken = fetchAccessToken(code);
        JsonNode userInfo = fetchUserInfo(accessToken);

        String giteeId = userInfo.get("id").asText();
        String login = userInfo.get("login").asText();
        String name = userInfo.has("name") && !userInfo.get("name").isNull()
                ? userInfo.get("name").asText() : login;
        String avatarUrl = userInfo.has("avatar_url") ? userInfo.get("avatar_url").asText() : null;

        User user = userRepository.findByGiteeId(giteeId).orElseGet(() -> {
            User newUser = new User();
            newUser.setGiteeId(giteeId);
            newUser.setOauthProvider("gitee");
            newUser.setUsername(resolveUsername(login));
            newUser.setDisplayName(name);
            newUser.setAvatar(avatarUrl);
            newUser.setRole("USER");
            newUser.setStatus(1);
            userRepository.insert(newUser);
            return newUser;
        });

        if (avatarUrl != null && !avatarUrl.equals(user.getAvatar())) {
            user.setAvatar(avatarUrl);
            userRepository.updateById(user);
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被封禁，请联系管理员");
        }

        String sessionId = sessionService.createSession(user, ip, userAgent);
        return new LoginResponse(sessionId, user.getId(), user.getUsername(), user.getAvatar(), user.getRole());
    }

    private String fetchAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://gitee.com/oauth/token",
                new HttpEntity<>(params, headers),
                String.class
        );

        try {
            JsonNode node = objectMapper.readTree(response.getBody());
            if (!node.has("access_token")) {
                throw new IllegalArgumentException("Gitee 授权失败：" + response.getBody());
            }
            return node.get("access_token").asText();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception e) {
            throw new RuntimeException("解析 Gitee token 失败", e);
        }
    }

    private JsonNode fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://gitee.com/api/v5/user",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        try {
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("解析 Gitee 用户信息失败", e);
        }
    }

    private String resolveUsername(String base) {
        String candidate = "gitee_" + base;
        int suffix = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = "gitee_" + base + "_" + suffix++;
        }
        return candidate;
    }
}
