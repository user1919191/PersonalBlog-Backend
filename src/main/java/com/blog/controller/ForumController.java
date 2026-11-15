package main.java.com.blog.controller;

import com.blog.dto.*;
import com.blog.service.ForumService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forum")
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/posts")
    public Result<PageResult<ForumPostDTO>> getPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "15") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(forumService.getPosts(pageNum, pageSize, keyword));
    }

    @GetMapping("/posts/{id}")
    public Result<ForumPostDTO> getPostById(@PathVariable Long id) {
        return Result.success(forumService.getPostById(id));
    }

    @PostMapping("/posts")
    public Result<ForumPostDTO> createPost(@Valid @RequestBody ForumPostRequest request,
                                            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success("发帖成功", forumService.createPost(request, userId));
    }

    @PostMapping("/replies")
    public Result<ForumReplyDTO> createReply(@Valid @RequestBody ForumReplyRequest request,
                                              HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success("回复成功", forumService.createReply(request, userId));
    }

    @DeleteMapping("/posts/{id}")
    public Result<Void> deletePost(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        forumService.deletePost(id, userId, role);
        return Result.success("删除成功", null);
    }

    @DeleteMapping("/replies/{id}")
    public Result<Void> deleteReply(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        forumService.deleteReply(id, userId, role);
        return Result.success("删除成功", null);
    }
}
