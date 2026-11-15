package main.java.com.blog.controller;

import com.blog.dto.PageResult;
import com.blog.dto.ReadingHistoryDTO;
import com.blog.dto.ReadingHistoryRequest;
import com.blog.dto.Result;
import com.blog.service.ReadingHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/reading-history")
public class UserReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    public UserReadingHistoryController(ReadingHistoryService readingHistoryService) {
        this.readingHistoryService = readingHistoryService;
    }

    @PutMapping("/articles/{articleId}")
    public Result<Void> save(@PathVariable Long articleId,
                             @Valid @RequestBody ReadingHistoryRequest request,
                             HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        readingHistoryService.save(userId, articleId, request.getLastAnchor());
        return Result.success("保存成功", null);
    }

    @GetMapping("/articles/{articleId}")
    public Result<Map<String, String>> getLastAnchor(@PathVariable Long articleId,
                                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(Map.of("lastAnchor", readingHistoryService.getLastAnchor(userId, articleId)));
    }

    @GetMapping("/continue")
    public Result<List<ReadingHistoryDTO>> getContinueReading(@RequestParam(defaultValue = "6") int limit,
                                                              HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(readingHistoryService.getContinueReading(userId, limit));
    }

    @GetMapping
    public Result<PageResult<ReadingHistoryDTO>> getReadingHistory(@RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "10") int pageSize,
                                                                   HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(readingHistoryService.getReadingHistory(userId, pageNum, pageSize));
    }
}
