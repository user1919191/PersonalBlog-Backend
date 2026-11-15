package main.java.com.blog.controller;

import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.Interview;
import com.blog.service.InterviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public Result<PageResult<Interview>> getInterviews(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        PageResult<Interview> result = interviewService.getInterviews(pageNum, pageSize, categoryId, keyword);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Interview> getInterviewById(@PathVariable Long id) {
        Interview interview = interviewService.getInterviewById(id);
        return Result.success(interview);
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeInterview(@PathVariable Long id, HttpServletRequest request) {
        String clientId = getClientId(request);
        interviewService.likeInterview(id, clientId);
        return Result.success("点赞成功", null);
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlikeInterview(@PathVariable Long id, HttpServletRequest request) {
        String clientId = getClientId(request);
        interviewService.unlikeInterview(id, clientId);
        return Result.success("取消点赞成功", null);
    }

    private String getClientId(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
