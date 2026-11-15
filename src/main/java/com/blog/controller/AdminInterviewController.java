package main.java.com.blog.controller;

import com.blog.dto.InterviewRequest;
import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.Interview;
import com.blog.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/interviews")
public class AdminInterviewController {

    private final InterviewService interviewService;

    public AdminInterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public Result<PageResult<Interview>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "15") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(interviewService.adminGetInterviews(pageNum, pageSize, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Interview> detail(@PathVariable Long id) {
        return Result.success(interviewService.getAdminInterviewById(id));
    }

    @PostMapping
    public Result<Interview> create(@Valid @RequestBody InterviewRequest request) {
        Interview interview = interviewService.createInterview(request);
        return Result.success("发布成功", interview);
    }

    @PutMapping("/{id}")
    public Result<Interview> update(@PathVariable Long id, @Valid @RequestBody InterviewRequest request) {
        Interview interview = interviewService.updateInterview(id, request);
        return Result.success("更新成功", interview);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        interviewService.deleteInterview(id);
        return Result.success("删除成功", null);
    }
}
