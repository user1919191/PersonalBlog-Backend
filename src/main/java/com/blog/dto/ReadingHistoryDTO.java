package main.java.com.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReadingHistoryDTO {
    private Long articleId;
    private String articleTitle;
    private String articleSummary;
    private String articleCoverImage;
    private String lastAnchor;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastReadAt;
}
