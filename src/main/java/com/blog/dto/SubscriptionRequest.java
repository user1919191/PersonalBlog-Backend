package main.java.com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SubscriptionRequest {

    @NotBlank(message = "订阅类型不能为空")
    private String type;

    @NotNull(message = "订阅目标不能为空")
    private Long targetId;
}
