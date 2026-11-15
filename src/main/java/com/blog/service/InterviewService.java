package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.InterviewRequest;
import com.blog.dto.PageResult;
import com.blog.entity.Interview;
import com.blog.repository.InterviewRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class InterviewService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";
    private static final String VISIBILITY_PRIVATE = "PRIVATE";

    private final InterviewRepository interviewRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SitemapService sitemapService;

    public InterviewService(InterviewRepository interviewRepository,
                            RedisTemplate<String, Object> redisTemplate,
                            SitemapService sitemapService) {
        this.interviewRepository = interviewRepository;
        this.redisTemplate = redisTemplate;
        this.sitemapService = sitemapService;
    }

    public PageResult<Interview> getInterviews(Integer pageNum, Integer pageSize, Long categoryId, String keyword) {
        Page<Interview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Interview::getStatus, 1)
                .eq(Interview::getVisibility, VISIBILITY_PUBLIC);

        if (categoryId != null) {
            wrapper.eq(Interview::getCategoryId, categoryId);
        }

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Interview::getTitle, keyword)
                    .or()
                    .like(Interview::getContent, keyword));
        }

        wrapper.orderByDesc(Interview::getIsTop, Interview::getCreateTime);

        Page<Interview> result = interviewRepository.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords());
    }

    public PageResult<Interview> adminGetInterviews(Integer pageNum, Integer pageSize, Long categoryId, String keyword) {
        Page<Interview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(Interview::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Interview::getTitle, keyword).or().like(Interview::getSummary, keyword));
        }
        wrapper.orderByDesc(Interview::getCreateTime);

        Page<Interview> result = interviewRepository.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords());
    }

    public Interview getInterviewById(Long id) {
        String cacheKey = "interview:" + id;
        Interview interview = (Interview) redisTemplate.opsForValue().get(cacheKey);

        if (interview == null) {
            interview = interviewRepository.selectById(id);
            assertPublicInterview(interview);
            redisTemplate.opsForValue().set(cacheKey, interview, 1, TimeUnit.HOURS);
        } else {
            assertPublicInterview(interview);
        }

        incrementViewCount(id);
        return interview;
    }

    public Interview getAdminInterviewById(Long id) {
        Interview interview = interviewRepository.selectById(id);
        if (interview == null) {
            throw new IllegalArgumentException("面经不存在");
        }
        return interview;
    }

    @Transactional
    public Interview createInterview(InterviewRequest request) {
        Interview interview = new Interview();
        applyRequest(interview, request);
        interview.setAuthorId(1L);
        interview.setViewCount(0);
        interview.setLikeCount(0);
        interview.setCommentCount(0);

        interviewRepository.insert(interview);
        sitemapService.triggerRefresh();
        return interview;
    }

    @Transactional
    public Interview updateInterview(Long id, InterviewRequest request) {
        Interview interview = interviewRepository.selectById(id);
        if (interview == null) {
            throw new IllegalArgumentException("面经不存在");
        }

        applyRequest(interview, request);
        interviewRepository.updateById(interview);
        redisTemplate.delete("interview:" + id);
        sitemapService.triggerRefresh();
        return interview;
    }

    @Transactional
    public void deleteInterview(Long id) {
        Interview interview = interviewRepository.selectById(id);
        if (interview == null) {
            throw new IllegalArgumentException("面经不存在");
        }
        interviewRepository.deleteById(id);
        redisTemplate.delete("interview:" + id);
        sitemapService.triggerRefresh();
    }

    @Transactional
    public void likeInterview(Long interviewId, String clientId) {
        String likeKey = "interview:like:" + interviewId + ":" + clientId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(likeKey))) {
            throw new IllegalArgumentException("请勿重复点赞");
        }

        Interview interview = requirePublicInterview(interviewId);
        redisTemplate.opsForValue().set(likeKey, "1", 7, TimeUnit.DAYS);
        interview.setLikeCount(interview.getLikeCount() + 1);
        interviewRepository.updateById(interview);
        redisTemplate.delete("interview:" + interviewId);
    }

    @Transactional
    public void unlikeInterview(Long interviewId, String clientId) {
        String likeKey = "interview:like:" + interviewId + ":" + clientId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(likeKey))) {
            throw new IllegalArgumentException("尚未点赞");
        }

        redisTemplate.delete(likeKey);

        Interview interview = requirePublicInterview(interviewId);
        interview.setLikeCount(Math.max(0, interview.getLikeCount() - 1));
        interviewRepository.updateById(interview);
        redisTemplate.delete("interview:" + interviewId);
    }

    private void incrementViewCount(Long interviewId) {
        String key = "interview:view:" + interviewId;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count % 100 == 0) {
            Interview interview = interviewRepository.selectById(interviewId);
            if (interview != null) {
                interview.setViewCount(interview.getViewCount() + 100);
                interviewRepository.updateById(interview);
            }
        }
    }

    private void applyRequest(Interview interview, InterviewRequest request) {
        interview.setTitle(request.getTitle());
        interview.setContent(request.getContent());
        interview.setSummary(request.getSummary());
        interview.setCoverImage(request.getCoverImage());
        interview.setCategoryId(request.getCategoryId());
        interview.setStatus(request.getStatus());
        interview.setVisibility(normalizeVisibility(request.getVisibility()));
        interview.setIsTop(request.getIsTop());
    }

    private Interview requirePublicInterview(Long interviewId) {
        Interview interview = interviewRepository.selectById(interviewId);
        assertPublicInterview(interview);
        return interview;
    }

    private void assertPublicInterview(Interview interview) {
        if (!isPublishedPublic(interview)) {
            throw new IllegalArgumentException("面经不存在");
        }
    }

    private boolean isPublishedPublic(Interview interview) {
        return interview != null
                && interview.getStatus() != null
                && interview.getStatus() == 1
                && VISIBILITY_PUBLIC.equals(interview.getVisibility());
    }

    private String normalizeVisibility(String visibility) {
        if (visibility == null || visibility.isBlank()) {
            return VISIBILITY_PUBLIC;
        }
        String normalized = visibility.trim().toUpperCase(Locale.ROOT);
        if (VISIBILITY_PUBLIC.equals(normalized) || VISIBILITY_PRIVATE.equals(normalized)) {
            return normalized;
        }
        throw new IllegalArgumentException("无效的可见性设置");
    }
}