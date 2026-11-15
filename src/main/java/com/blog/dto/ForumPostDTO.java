package main.java.com.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ForumPostDTO {
    private Long id;
    private String title;
    private String content;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Integer viewCount;
    private Integer replyCount;
    private Integer isTop;
    private Integer status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastReplyTime;
    private List<ForumReplyDTO> replies;
}
