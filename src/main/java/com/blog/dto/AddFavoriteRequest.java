package main.java.com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFavoriteRequest {
    @NotNull(message = "收藏夹ID不能为空")
    private Long folderId;

    @NotBlank(message = "类型不能为空")
    private String itemType; // article / featured / interview

    @NotNull(message = "内容ID不能为空")
    private Long itemId;

    private String learningStatus = "UNREAD";
}
