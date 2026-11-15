package main.java.com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.ForumPostDTO;
import com.blog.dto.ForumReplyDTO;
import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.ForumPost;
import com.blog.entity.ForumReply;
import com.blog.entity.User;
import com.blog.repository.ForumPostRepository;
import com.blog.repository.ForumReplyRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ForumService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/forum")
public class AdminForumController {

    private final ForumPostRepository forumPostRepository;
    private final ForumReplyRepository forumReplyRepository;
    private final UserRepository userRepository;
    private final ForumService forumService;

    public AdminForumController(ForumPostRepository forumPostRepository,
                                ForumReplyRepository forumReplyRepository,
                                UserRepository userRepository,
                                ForumService forumService) {
        this.forumPostRepository = forumPostRepository;
        this.forumReplyRepository = forumReplyRepository;
        this.userRepository = userRepository;
        this.forumService = forumService;
    }

    /** 管理员分页查看所有帖子（含已删除） */
    @GetMapping("/posts")
    public Result<PageResult<ForumPostDTO>> listPosts(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(forumService.adminGetPosts(pageNum, pageSize, keyword, status));
    }

    /** 切换置顶状态 */
    @PatchMapping("/posts/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long id) {
        ForumPost post = forumPostRepository.selectById(id);
        if (post == null) throw new IllegalArgumentException("帖子不存在");
        post.setIsTop(post.getIsTop() == 1 ? 0 : 1);
        forumPostRepository.updateById(post);
        return Result.success("操作成功", null);
    }

    /** 软删除 / 恢复帖子 */
    @PatchMapping("/posts/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        ForumPost post = forumPostRepository.selectById(id);
        if (post == null) throw new IllegalArgumentException("帖子不存在");
        post.setStatus(status);
        forumPostRepository.updateById(post);
        return Result.success("操作成功", null);
    }

    /** 查询某帖子下的所有回复（含已删除） */
    @GetMapping("/posts/{postId}/replies")
    public Result<List<ForumReplyDTO>> listReplies(@PathVariable Long postId) {
        List<ForumReply> replies = forumReplyRepository.selectList(
            new LambdaQueryWrapper<ForumReply>()
                .eq(ForumReply::getPostId, postId)
                .orderByAsc(ForumReply::getCreateTime)
        );
        List<ForumReplyDTO> dtos = replies.stream().map(r -> {
            ForumReplyDTO dto = new ForumReplyDTO();
            dto.setId(r.getId());
            dto.setPostId(r.getPostId());
            dto.setParentId(r.getParentId());
            dto.setUserId(r.getUserId());
            dto.setContent(r.getContent());
            dto.setCreateTime(r.getCreateTime());
            dto.setStatus(r.getStatus());
            User user = r.getUserId() != null ? userRepository.selectById(r.getUserId()) : null;
            if (user != null) {
                dto.setAuthorName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
                dto.setAuthorAvatar(user.getAvatar());
            }
            return dto;
        }).collect(Collectors.toList());
        return Result.success(dtos);
    }

    /** 删除单条回复（软删除） */
    @DeleteMapping("/replies/{id}")
    public Result<Void> deleteReply(@PathVariable Long id) {
        ForumReply reply = forumReplyRepository.selectById(id);
        if (reply == null) throw new IllegalArgumentException("回复不存在");
        reply.setStatus(0);
        forumReplyRepository.updateById(reply);
        // 同步减少帖子回复数
        ForumPost post = forumPostRepository.selectById(reply.getPostId());
        if (post != null && post.getReplyCount() > 0) {
            post.setReplyCount(post.getReplyCount() - 1);
            forumPostRepository.updateById(post);
        }
        return Result.success("删除成功", null);
    }
}

