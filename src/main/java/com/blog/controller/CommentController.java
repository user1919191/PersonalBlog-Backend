package main.java.com.blog.controller;

import com.blog.dto.CommentDTO;
import com.blog.dto.CommentRequest;
import com.blog.dto.Result;
import com.blog.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long RATE_LIMIT_SECONDS = 60L;

    public CommentController(CommentService commentService,
                             RedisTemplate<String, Object> redisTemplate) {
        this.commentService = commentService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/article/{articleId}")
    public Result<List<CommentDTO>> getCommentsByArticleId(@PathVariable Long articleId) {
        return Result.success(commentService.getCommentsByArticleId(articleId));
    }

    @PostMapping
    public Result<CommentDTO> createComment(@Valid @RequestBody CommentRequest request,
                                             HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 每用户每 60 秒限发一条评论
        String rateLimitKey = "comment:limit:user:" + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey))) {
            Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
            throw new IllegalArgumentException(
                "评论过于频繁，请 " + (ttl != null ? ttl : RATE_LIMIT_SECONDS) + " 秒后再试");
        }
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        return Result.success("评论成功", commentService.createComment(request, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success("删除成功", null);
    }
}
