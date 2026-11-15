package main.java.com.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserDTO {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String avatar;
    private String role;
    private Integer status;
    private String oauthProvider;
    private String giteeId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private long sessionCount;
}
