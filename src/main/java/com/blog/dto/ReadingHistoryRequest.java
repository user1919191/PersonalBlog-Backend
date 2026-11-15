package main.java.com.blog.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReadingHistoryRequest {

    @Size(max = 255, message = "锚点长度不能超过255")
    private String lastAnchor;
}
