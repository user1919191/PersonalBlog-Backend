package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.ForumPostDTO;
import com.blog.dto.ForumPostRequest;
import com.blog.dto.ForumReplyDTO;
import com.blog.dto.ForumReplyRequest;
import com.blog.dto.PageResult;
import com.blog.entity.ForumPost;
import com.blog.entity.ForumReply;
import com.blog.entity.User;
import com.blog.repository.ForumPostRepository;
import com.blog.repository.ForumReplyRepository;
import com.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumService {

    private final ForumPostRepository forumPostRepository;
    private final ForumReplyRepository forumReplyRepository;
    private final UserRepository userRepository;

    public ForumService(ForumPostRepository forumPostRepository,
                        ForumReplyRepository forumReplyRepository,
                        UserRepository userRepository) {
        this.forumPostRepository = forumPostRepository;
        this.forumReplyRepository = forumReplyRepository;
        this.userRepository = userRepository;
    }

    public PageResult<ForumPostDTO> getPosts(Integer pageNum, Integer pageSize, String keyword) {
        Page<ForumPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumPost::getStatus, 1);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(ForumPost::getTitle, keyword).or().like(ForumPost::getContent, keyword));
        }
        wrapper.orderByDesc(ForumPost::getIsTop, ForumPost::getLastReplyTime, ForumPost::getCreateTime);

        Page<ForumPost> result = forumPostRepository.selectPage(page, wrapper);
        List<ForumPostDTO> dtos = result.getRecords().stream()
                .map(p -> toPostDTO(p, false))
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), pageNum, pageSize, dtos);
    }

    /** 管理员查询：可按 status 过滤，null 时查全部 */
    public PageResult<ForumPostDTO> adminGetPosts(Integer pageNum, Integer pageSize,
                                                   String keyword, Integer status) {
        Page<ForumPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ForumPost::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(ForumPost::getTitle, keyword).or().like(ForumPost::getContent, keyword));
        }
        wrapper.orderByDesc(ForumPost::getCreateTime);

        Page<ForumPost> result = forumPostRepository.selectPage(page, wrapper);
        List<ForumPostDTO> dtos = result.getRecords().stream()
                .map(p -> toPostDTO(p, false))
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), pageNum, pageSize, dtos);
    }

    public ForumPostDTO getPostById(Long id) {
        ForumPost post = forumPostRepository.selectById(id);
        if (post == null || post.getStatus() == 0) {
            throw new IllegalArgumentException("帖子不存在");
        }
        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        forumPostRepository.updateById(post);

        ForumPostDTO dto = toPostDTO(post, true);

        // 加载所有回复
        LambdaQueryWrapper<ForumReply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumReply::getPostId, id)
               .eq(ForumReply::getStatus, 1)
               .orderByAsc(ForumReply::getCreateTime);
        List<ForumReplyDTO> replies = forumReplyRepository.selectList(wrapper).stream()
                .map(this::toReplyDTO)
                .collect(Collectors.toList());
        dto.setReplies(replies);
        return dto;
    }

    @Transactional
    public ForumPostDTO createPost(ForumPostRequest request, Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");

        ForumPost post = new ForumPost();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUserId(userId);
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setIsTop(0);
        post.setStatus(1);
        post.setLastReplyTime(LocalDateTime.now());
        forumPostRepository.insert(post);
        return toPostDTO(post, false);
    }

    @Transactional
    public ForumReplyDTO createReply(ForumReplyRequest request, Long userId) {
        ForumPost post = forumPostRepository.selectById(request.getPostId());
        if (post == null || post.getStatus() == 0) throw new IllegalArgumentException("帖子不存在");

        User user = userRepository.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");

        ForumReply reply = new ForumReply();
        reply.setPostId(request.getPostId());
        reply.setUserId(userId);
        reply.setContent(request.getContent());
        reply.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        reply.setStatus(1);
        forumReplyRepository.insert(reply);

        post.setReplyCount(post.getReplyCount() + 1);
        post.setLastReplyTime(LocalDateTime.now());
        forumPostRepository.updateById(post);
        return toReplyDTO(reply, user);
    }

    @Transactional
    public void deletePost(Long id, Long userId, String role) {
        ForumPost post = forumPostRepository.selectById(id);
        if (post == null) throw new IllegalArgumentException("帖子不存在");
        if (!"ADMIN".equals(role) && !post.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除此帖子");
        }
        post.setStatus(0);
        forumPostRepository.updateById(post);
    }

    @Transactional
    public void deleteReply(Long id, Long userId, String role) {
        ForumReply reply = forumReplyRepository.selectById(id);
        if (reply == null) throw new IllegalArgumentException("回复不存在");
        if (!"ADMIN".equals(role) && !reply.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除此回复");
        }
        reply.setStatus(0);
        forumReplyRepository.updateById(reply);

        ForumPost post = forumPostRepository.selectById(reply.getPostId());
        if (post != null) {
            post.setReplyCount(Math.max(0, post.getReplyCount() - 1));
            forumPostRepository.updateById(post);
        }
    }

    private ForumPostDTO toPostDTO(ForumPost post, boolean includeContent) {
        ForumPostDTO dto = new ForumPostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        if (includeContent) dto.setContent(post.getContent());
        dto.setUserId(post.getUserId());
        dto.setViewCount(post.getViewCount());
        dto.setReplyCount(post.getReplyCount());
        dto.setIsTop(post.getIsTop());
        dto.setStatus(post.getStatus());
        dto.setCreateTime(post.getCreateTime());
        dto.setLastReplyTime(post.getLastReplyTime());

        User user = post.getUserId() != null ? userRepository.selectById(post.getUserId()) : null;
        if (user != null) {
            dto.setAuthorName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
            dto.setAuthorAvatar(user.getAvatar());
        }
        return dto;
    }

    private ForumReplyDTO toReplyDTO(ForumReply reply) {
        User user = reply.getUserId() != null ? userRepository.selectById(reply.getUserId()) : null;
        return toReplyDTO(reply, user);
    }

    private ForumReplyDTO toReplyDTO(ForumReply reply, User user) {
        ForumReplyDTO dto = new ForumReplyDTO();
        dto.setId(reply.getId());
        dto.setPostId(reply.getPostId());
        dto.setParentId(reply.getParentId());
        dto.setUserId(reply.getUserId());
        dto.setContent(reply.getContent());
        dto.setCreateTime(reply.getCreateTime());
        if (user != null) {
            dto.setAuthorName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
            dto.setAuthorAvatar(user.getAvatar());
        }
        return dto;
    }
}
