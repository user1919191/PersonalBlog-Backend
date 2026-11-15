package main.java.com.blog.controller;

import com.blog.dto.InterviewCommentDTO;
import com.blog.dto.InterviewCommentRequest;
import com.blog.dto.Result;
import com.blog.service.InterviewCommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interview-comments")
public class InterviewCommentController {

    private final InterviewCommentService interviewCommentService;

    public InterviewCommentController(InterviewCommentService interviewCommentService) {
        this.interviewCommentService = interviewCommentService;
    }

    @GetMapping("/interview/{interviewId}")
    public Result<List<InterviewCommentDTO>> getComments(@PathVariable Long interviewId) {
        return Result.success(interviewCommentService.getCommentsByInterviewId(interviewId));
    }

    @PostMapping
    public Result<InterviewCommentDTO> createComment(@Valid @RequestBody InterviewCommentRequest request,
                                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success("评论成功", interviewCommentService.createComment(request, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        interviewCommentService.deleteComment(id);
        return Result.success("删除成功", null);
    }
}
