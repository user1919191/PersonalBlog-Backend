package main.java.com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("featured_article")
public class FeaturedArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String url;

    private String category;

    private String description;

    private String coverImage;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
