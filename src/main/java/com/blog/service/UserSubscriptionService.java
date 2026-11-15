package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.UserSubscriptionDTO;
import com.blog.entity.Tag;
import com.blog.entity.User;
import com.blog.entity.UserSubscription;
import com.blog.repository.TagRepository;
import com.blog.repository.UserRepository;
import com.blog.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public UserSubscriptionService(UserSubscriptionRepository subscriptionRepository,
                                   UserRepository userRepository,
                                   TagRepository tagRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional
    public void subscribe(Long userId, String type, Long targetId) {
        String normalizedType = normalizeType(type);
        UserSubscription existing = subscriptionRepository.selectOne(
                new LambdaQueryWrapper<UserSubscription>()
                        .eq(UserSubscription::getUserId, userId)
                        .eq(UserSubscription::getType, normalizedType)
                        .eq(UserSubscription::getTargetId, targetId)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            return;
        }

        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(userId);
        subscription.setType(normalizedType);
        subscription.setTargetId(targetId);
        subscription.setMuted(0);
        subscriptionRepository.insert(subscription);
    }

    @Transactional
    public void unsubscribe(Long userId, String type, Long targetId) {
        subscriptionRepository.delete(
                new LambdaQueryWrapper<UserSubscription>()
                        .eq(UserSubscription::getUserId, userId)
                        .eq(UserSubscription::getType, normalizeType(type))
                        .eq(UserSubscription::getTargetId, targetId)
        );
    }

    public List<UserSubscriptionDTO> getSubscriptions(Long userId) {
        List<UserSubscription> subscriptions = subscriptionRepository.selectList(
                new LambdaQueryWrapper<UserSubscription>()
                        .eq(UserSubscription::getUserId, userId)
                        .orderByDesc(UserSubscription::getCreateTime)
        );
        if (subscriptions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> authorIds = subscriptions.stream()
                .filter(item -> "author".equals(item.getType()))
                .map(UserSubscription::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        List<Long> tagIds = subscriptions.stream()
                .filter(item -> "tag".equals(item.getType()))
                .map(UserSubscription::getTargetId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = authorIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.selectBatchIds(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Map<Long, Tag> tagMap = tagIds.isEmpty()
                ? Collections.emptyMap()
                : tagRepository.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));

        return subscriptions.stream().map(item -> {
            UserSubscriptionDTO dto = new UserSubscriptionDTO();
            dto.setId(item.getId());
            dto.setType(item.getType());
            dto.setTargetId(item.getTargetId());
            dto.setCreateTime(item.getCreateTime());
            if ("author".equals(item.getType())) {
                User user = userMap.get(item.getTargetId());
                dto.setTargetName(user != null
                        ? (user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                        : "未知作者");
            } else {
                Tag tag = tagMap.get(item.getTargetId());
                dto.setTargetName(tag != null ? tag.getName() : "未知标签");
            }
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Long> findSubscriberIds(String type, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Collections.emptyList();
        }
        return subscriptionRepository.selectList(
                new LambdaQueryWrapper<UserSubscription>()
                        .eq(UserSubscription::getType, normalizeType(type))
                        .in(UserSubscription::getTargetId, targetIds)
        ).stream().map(UserSubscription::getUserId).distinct().collect(Collectors.toList());
    }

    private String normalizeType(String type) {
        if (!"tag".equals(type) && !"author".equals(type)) {
            throw new IllegalArgumentException("不支持的订阅类型");
        }
        return type;
    }
}
