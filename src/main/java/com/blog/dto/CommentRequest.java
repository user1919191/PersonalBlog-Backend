package main.java.com.blog.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class CommentRequest {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    private Long parentId = 0L;

    @NotBlank(message = "评论内容不能为空")
    @Size(min = 5, max = 500, message = "评论内容长度需在 5~500 字符之间")
    private String content;
}
