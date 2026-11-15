package main.java.com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionUser {
    private Long userId;
    private String username;
    private String role;
    private String displayName;
    private String avatar;
    // IP / UA 异常检测（Item 3）
    private String createdIp;
    private String lastIp;
    private String userAgent;
    private String createdAt;
    private String lastUsedAt;
}
