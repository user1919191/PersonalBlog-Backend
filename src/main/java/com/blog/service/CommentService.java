package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.CommentDTO;
import com.blog.dto.CommentRequest;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository,
                          ArticleRepository articleRepository,
                          UserRepository userRepository,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<CommentDTO> getCommentsByArticleId(Long articleId) {
        requirePublicArticle(articleId);
        return commentRepository.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, 1)
                        .orderByAsc(Comment::getCreateTime)
        ).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public CommentDTO createComment(CommentRequest request, Long userId) {
        Article article = requirePublicArticle(request.getArticleId());

        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setParentId(request.getParentId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentRepository.insert(comment);

        article.setCommentCount(article.getCommentCount() + 1);
        articleRepository.updateById(article);
        notificationService.notifyCommentReply(comment);
        return convertToDTO(comment, user);
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.selectById(id);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        comment.setStatus(0);
        commentRepository.updateById(comment);
        Article article = articleRepository.selectById(comment.getArticleId());
        if (article != null) {
            article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
            articleRepository.updateById(article);
        }
    }

    private CommentDTO convertToDTO(Comment comment) {
        User user = comment.getUserId() != null ? userRepository.selectById(comment.getUserId()) : null;
        return convertToDTO(comment, user);
    }

    private CommentDTO convertToDTO(Comment comment, User user) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setArticleId(comment.getArticleId());
        dto.setParentId(comment.getParentId());
        dto.setUserId(comment.getUserId());
        dto.setContent(comment.getContent());
        dto.setLikeCount(comment.getLikeCount());
        dto.setCreateTime(comment.getCreateTime());
        if (user != null) {
            dto.setDisplayName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
            dto.setAvatar(user.getAvatar());
        } else {
            dto.setDisplayName(comment.getNickname());
        }
        return dto;
    }

    private Article requirePublicArticle(Long articleId) {
        Article article = articleRepository.selectById(articleId);
        if (article == null
                || article.getStatus() == null
                || article.getStatus() != 1
                || !VISIBILITY_PUBLIC.equals(article.getVisibility())) {
            throw new IllegalArgumentException("文章不存在");
        }
        return article;
    }
}