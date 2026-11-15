package main.java.com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameFolderRequest {

    @NotBlank(message = "folder name is required")
    @Size(max = 50, message = "folder name must be <= 50 characters")
    private String name;
}