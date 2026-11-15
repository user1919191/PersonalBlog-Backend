package main.java.com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForumPostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 200, message = "标题长度需在 5~200 字符之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 10, max = 10000, message = "内容长度需在 10~10000 字符之间")
    private String content;
}
