package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.NotificationDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.Notification;
import com.blog.entity.User;
import com.blog.entity.UserNotification;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.NotificationRepository;
import com.blog.repository.UserNotificationRepository;
import com.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserSubscriptionService userSubscriptionService;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               UserNotificationRepository userNotificationRepository,
                               UserSubscriptionService userSubscriptionService,
                               UserRepository userRepository,
                               ArticleRepository articleRepository,
                               CommentRepository commentRepository) {
        this.notificationRepository = notificationRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.userSubscriptionService = userSubscriptionService;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void notifyArticlePublished(Article article, List<Long> tagIds) {
        if (article.getStatus() == null || article.getStatus() != 1) {
            return;
        }
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.addAll(userSubscriptionService.findSubscriberIds("author", Collections.singletonList(article.getAuthorId())));
        recipients.addAll(userSubscriptionService.findSubscriberIds("tag", tagIds));
        recipients.remove(article.getAuthorId());
        if (recipients.isEmpty()) {
            return;
        }

        String authorName = getUserDisplayName(article.getAuthorId());
        publishToUsers(recipients, buildNotification(
                "article_publish",
                authorName + " 发布了新文章",
                article.getTitle(),
                "/article/" + article.getId(),
                article.getAuthorId(),
                authorName,
                "article",
                article.getId()
        ));
    }

    @Transactional
    public void notifyArticleUpdated(Article article, List<Long> tagIds) {
        if (article.getStatus() == null || article.getStatus() != 1) {
            return;
        }
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.addAll(userSubscriptionService.findSubscriberIds("author", Collections.singletonList(article.getAuthorId())));
        recipients.addAll(userSubscriptionService.findSubscriberIds("tag", tagIds));
        recipients.remove(article.getAuthorId());
        if (recipients.isEmpty()) {
            return;
        }

        String authorName = getUserDisplayName(article.getAuthorId());
        publishToUsers(recipients, buildNotification(
                "article_update",
                authorName + " 更新了文章",
                article.getTitle(),
                "/article/" + article.getId(),
                article.getAuthorId(),
                authorName,
                "article",
                article.getId()
        ));
    }

    @Transactional
    public void notifyArticleLiked(Long articleId, Long actorId, String actorName) {
        Article article = articleRepository.selectById(articleId);
        if (article == null || article.getAuthorId() == null) {
            return;
        }
        if (actorId != null && actorId.equals(article.getAuthorId())) {
            return;
        }

        publishToUsers(Collections.singleton(article.getAuthorId()), buildNotification(
                "article_like",
                safeActorName(actorId, actorName) + " 点赞了你的文章",
                article.getTitle(),
                "/article/" + article.getId(),
                actorId,
                safeActorName(actorId, actorName),
                "article",
                article.getId()
        ));
    }

    @Transactional
    public void notifyCommentReply(Comment comment) {
        if (comment.getParentId() == null || comment.getParentId() == 0) {
            return;
        }
        Comment parent = commentRepository.selectById(comment.getParentId());
        if (parent == null || parent.getUserId() == null || parent.getUserId().equals(comment.getUserId())) {
            return;
        }

        Article article = articleRepository.selectById(comment.getArticleId());
        String actorName = getUserDisplayName(comment.getUserId());
        publishToUsers(Collections.singleton(parent.getUserId()), buildNotification(
                "comment_reply",
                actorName + " 回复了你的评论",
                article != null ? article.getTitle() : "文章评论",
                article != null ? "/article/" + article.getId() : "/",
                comment.getUserId(),
                actorName,
                "comment",
                comment.getId()
        ));
    }

    public List<NotificationDTO> getNotifications(Long userId, int limit) {
        List<UserNotification> userNotifications = userNotificationRepository.selectList(
                new LambdaQueryWrapper<UserNotification>()
                        .eq(UserNotification::getUserId, userId)
                        .eq(UserNotification::getIsDeleted, 0)
                        .orderByDesc(UserNotification::getCreateTime)
                        .last("LIMIT " + Math.min(Math.max(limit, 1), 50))
        );
        if (userNotifications.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> notificationIds = userNotifications.stream()
                .map(UserNotification::getNotificationId)
                .distinct()
                .collect(Collectors.toList());
        if (notificationIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Notification> notificationMap = notificationRepository.selectBatchIds(notificationIds).stream()
                .collect(Collectors.toMap(Notification::getId, Function.identity()));

        return userNotifications.stream()
                .map(item -> {
                    Notification notification = notificationMap.get(item.getNotificationId());
                    if (notification == null) {
                        return null;
                    }
                    NotificationDTO dto = new NotificationDTO();
                    dto.setId(item.getId());
                    dto.setType(notification.getType());
                    dto.setTitle(notification.getTitle());
                    dto.setContent(notification.getContent());
                    dto.setLink(notification.getLink());
                    dto.setActorName(notification.getActorName());
                    dto.setIsRead(item.getIsRead());
                    dto.setCreateTime(notification.getCreateTime());
                    return dto;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        Long count = userNotificationRepository.selectCount(
                new LambdaQueryWrapper<UserNotification>()
                        .eq(UserNotification::getUserId, userId)
                        .eq(UserNotification::getIsDeleted, 0)
                        .eq(UserNotification::getIsRead, 0)
        );
        return count != null ? count : 0;
    }

    @Transactional
    public void markRead(Long userId, Long userNotificationId) {
        UserNotification userNotification = userNotificationRepository.selectById(userNotificationId);
        if (userNotification == null || !userId.equals(userNotification.getUserId())) {
            throw new IllegalArgumentException("通知不存在");
        }
        userNotification.setIsRead(1);
        userNotification.setReadAt(LocalDateTime.now());
        userNotificationRepository.updateById(userNotification);
    }

    @Transactional
    public void markAllRead(Long userId) {
        List<UserNotification> unreadList = userNotificationRepository.selectList(
                new LambdaQueryWrapper<UserNotification>()
                        .eq(UserNotification::getUserId, userId)
                        .eq(UserNotification::getIsDeleted, 0)
                        .eq(UserNotification::getIsRead, 0)
        );
        LocalDateTime now = LocalDateTime.now();
        for (UserNotification item : unreadList) {
            item.setIsRead(1);
            item.setReadAt(now);
            userNotificationRepository.updateById(item);
        }
    }

    private Notification buildNotification(String type,
                                           String title,
                                           String content,
                                           String link,
                                           Long actorId,
                                           String actorName,
                                           String targetType,
                                           Long targetId) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLink(link);
        notification.setActorId(actorId);
        notification.setActorName(actorName);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        return notification;
    }

    private void publishToUsers(Collection<Long> recipientIds, Notification notification) {
        List<Long> recipients = recipientIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .collect(Collectors.toList());
        if (recipients.isEmpty()) {
            return;
        }

        notificationRepository.insert(notification);
        for (Long userId : recipients) {
            UserNotification userNotification = new UserNotification();
            userNotification.setUserId(userId);
            userNotification.setNotificationId(notification.getId());
            userNotification.setIsRead(0);
            userNotification.setIsDeleted(0);
            userNotificationRepository.insert(userNotification);
        }
    }

    private String getUserDisplayName(Long userId) {
        if (userId == null) {
            return "系统";
        }
        User user = userRepository.selectById(userId);
        if (user == null) {
            return "用户";
        }
        return user.getDisplayName() != null && !user.getDisplayName().isBlank()
                ? user.getDisplayName()
                : user.getUsername();
    }

    private String safeActorName(Long actorId, String actorName) {
        if (actorName != null && !actorName.isBlank()) {
            return actorName;
        }
        return actorId != null ? getUserDisplayName(actorId) : "访客";
    }
}
