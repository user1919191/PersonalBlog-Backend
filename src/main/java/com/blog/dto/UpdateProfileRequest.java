package main.java.com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotBlank(message = "显示名不能为空")
    @Size(max = 50, message = "显示名不超过50字符")
    private String displayName;
}
