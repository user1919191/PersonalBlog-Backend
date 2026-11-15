package main.java.com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("interview_comment")
public class InterviewComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long interviewId;

    private Long parentId;

    private Long userId;

    private String content;

    private String nickname;

    private String email;

    private Integer likeCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
