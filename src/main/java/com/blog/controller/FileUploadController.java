package main.java.com.blog.controller;

import com.blog.dto.Result;
import com.blog.service.CosService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/upload")
public class FileUploadController {

    private final CosService cosService;

    public FileUploadController(CosService cosService) {
        this.cosService = cosService;
    }

    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        String url = cosService.uploadFile(file);
        return Result.success(url);
    }
}
