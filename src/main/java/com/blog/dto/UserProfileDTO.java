package main.java.com.blog.dto;

import lombok.Data;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String displayName;
    private String avatar;
    private String email;
}
