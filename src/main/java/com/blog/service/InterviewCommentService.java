package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.InterviewCommentDTO;
import com.blog.dto.InterviewCommentRequest;
import com.blog.entity.Interview;
import com.blog.entity.InterviewComment;
import com.blog.entity.User;
import com.blog.repository.InterviewCommentRepository;
import com.blog.repository.InterviewRepository;
import com.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InterviewCommentService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    private final InterviewCommentRepository interviewCommentRepository;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;

    public InterviewCommentService(InterviewCommentRepository interviewCommentRepository,
                                   InterviewRepository interviewRepository,
                                   UserRepository userRepository) {
        this.interviewCommentRepository = interviewCommentRepository;
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
    }

    public List<InterviewCommentDTO> getCommentsByInterviewId(Long interviewId) {
        requirePublicInterview(interviewId);
        LambdaQueryWrapper<InterviewComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewComment::getInterviewId, interviewId)
                .eq(InterviewComment::getStatus, 1)
                .orderByAsc(InterviewComment::getCreateTime);
        return interviewCommentRepository.selectList(wrapper).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public InterviewCommentDTO createComment(InterviewCommentRequest request, Long userId) {
        Interview interview = requirePublicInterview(request.getInterviewId());

        User user = userRepository.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        InterviewComment comment = new InterviewComment();
        comment.setInterviewId(request.getInterviewId());
        comment.setParentId(request.getParentId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        interviewCommentRepository.insert(comment);

        interview.setCommentCount(interview.getCommentCount() + 1);
        interviewRepository.updateById(interview);

        return convertToDTO(comment, user);
    }

    @Transactional
    public void deleteComment(Long id) {
        InterviewComment comment = interviewCommentRepository.selectById(id);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        comment.setStatus(0);
        interviewCommentRepository.updateById(comment);
        Interview interview = interviewRepository.selectById(comment.getInterviewId());
        if (interview != null) {
            interview.setCommentCount(Math.max(0, interview.getCommentCount() - 1));
            interviewRepository.updateById(interview);
        }
    }

    private InterviewCommentDTO convertToDTO(InterviewComment comment) {
        User user = comment.getUserId() != null ? userRepository.selectById(comment.getUserId()) : null;
        return convertToDTO(comment, user);
    }

    private InterviewCommentDTO convertToDTO(InterviewComment comment, User user) {
        InterviewCommentDTO dto = new InterviewCommentDTO();
        dto.setId(comment.getId());
        dto.setInterviewId(comment.getInterviewId());
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

    private Interview requirePublicInterview(Long interviewId) {
        Interview interview = interviewRepository.selectById(interviewId);
        if (interview == null
                || interview.getStatus() == null
                || interview.getStatus() != 1
                || !VISIBILITY_PUBLIC.equals(interview.getVisibility())) {
            throw new IllegalArgumentException("面经不存在");
        }
        return interview;
    }
}