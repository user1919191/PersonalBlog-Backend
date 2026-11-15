package main.java.com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.blog.dto.ArticleSeriesDTO;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private String summary;

    private String coverImage;

    private Long categoryId;

    private Long authorId;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Integer status;

    private String visibility;

    private Integer isTop;

    /** 所属专栏ID（可为空） */
    private Long seriesId;

    /** 在专栏中的顺序 */
    private Integer seriesOrder;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;

    /** 标签列表（非数据库字段，查询时附加） */
    @TableField(exist = false)
    private List<Tag> tags;

    /** 专栏导航（非数据库字段，文章详情时附加） */
    @TableField(exist = false)
    private ArticleSeriesDTO.SeriesNavigation seriesNav;

    @TableField(exist = false)
    private ArticleSeriesDTO series;

    @TableField(exist = false)
    private String authorName;
}
