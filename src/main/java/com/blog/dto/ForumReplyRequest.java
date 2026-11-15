package main.java.com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForumReplyRequest {

    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    private Long parentId = 0L;

    @NotBlank(message = "回复内容不能为空")
    @Size(min = 2, max = 2000, message = "回复内容长度需在 2~2000 字符之间")
    private String content;
}
