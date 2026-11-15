package main.java.com.blog.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class InterviewCommentRequest {

    @NotNull(message = "面经ID不能为空")
    private Long interviewId;

    private Long parentId = 0L;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
