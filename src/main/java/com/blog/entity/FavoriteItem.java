package main.java.com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@TableName("favorite_item")
public class FavoriteItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long folderId;

    private String itemType; // article / featured / interview

    private Long itemId;

    private String learningStatus;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;
}
