package main.java.com.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleSeriesDTO {
    private Long id;
    private String name;
    private String description;
    private String coverImage;
    private Integer sortOrder;
    private Integer articleCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    /** 专栏内的有序文章列表（仅 getSeriesById 时填充） */
    private List<SeriesArticleItem> articles;

    @Data
    public static class SeriesArticleItem {
        private Long id;
        private String title;
        private Integer seriesOrder;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
        private LocalDateTime createTime;
    }

    @Data
    public static class SeriesNavigation {
        private Long seriesId;
        private String seriesName;
        private Integer currentOrder;
        private Integer totalCount;
        private Long prevId;
        private String prevTitle;
        private Long nextId;
        private String nextTitle;
    }
}
