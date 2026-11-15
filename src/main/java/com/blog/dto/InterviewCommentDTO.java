package main.java.com.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InterviewCommentDTO {

    private Long id;
    private Long interviewId;
    private Long parentId;
    private Long userId;
    private String displayName;
    private String avatar;
    private String content;
    private Integer likeCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
