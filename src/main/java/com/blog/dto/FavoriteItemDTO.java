package main.java.com.blog.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class FavoriteItemDTO {
    private Long id;
    private Long folderId;
    private String itemType;
    private Long itemId;
    private String title;
    private String url;
    private String coverImage;
    private String learningStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
