package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.PageResult;
import com.blog.dto.ReadingHistoryDTO;
import com.blog.entity.Article;
import com.blog.entity.UserReadingHistory;
import com.blog.repository.ArticleRepository;
import com.blog.repository.UserReadingHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReadingHistoryService {

    private static final int RETENTION_DAYS = 15;
    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    private final UserReadingHistoryRepository historyRepository;
    private final ArticleRepository articleRepository;

    public ReadingHistoryService(UserReadingHistoryRepository historyRepository,
                                 ArticleRepository articleRepository) {
        this.historyRepository = historyRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public void save(Long userId, Long articleId, String lastAnchor) {
        cleanupExpired(userId);
        UserReadingHistory history = historyRepository.selectOne(
                new LambdaQueryWrapper<UserReadingHistory>()
                        .eq(UserReadingHistory::getUserId, userId)
                        .eq(UserReadingHistory::getArticleId, articleId)
                        .ge(UserReadingHistory::getLastReadAt, cutoffTime())
                        .last("LIMIT 1")
        );

        if (history == null) {
            history = new UserReadingHistory();
            history.setUserId(userId);
            history.setArticleId(articleId);
            history.setLastAnchor(normalizeAnchor(lastAnchor));
            history.setLastReadAt(LocalDateTime.now());
            historyRepository.insert(history);
            return;
        }

        history.setLastAnchor(normalizeAnchor(lastAnchor));
        history.setLastReadAt(LocalDateTime.now());
        historyRepository.updateById(history);
    }

    public String getLastAnchor(Long userId, Long articleId) {
        cleanupExpired(userId);
        UserReadingHistory history = historyRepository.selectOne(
                new LambdaQueryWrapper<UserReadingHistory>()
                        .eq(UserReadingHistory::getUserId, userId)
                        .eq(UserReadingHistory::getArticleId, articleId)
                        .ge(UserReadingHistory::getLastReadAt, cutoffTime())
                        .last("LIMIT 1")
        );
        return history != null ? history.getLastAnchor() : "";
    }

    public List<ReadingHistoryDTO> getContinueReading(Long userId, int limit) {
        return buildHistory(userId, Math.min(Math.max(limit, 1), 10));
    }

    public PageResult<ReadingHistoryDTO> getReadingHistory(Long userId, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 20);
        cleanupExpired(userId);

        Page<UserReadingHistory> page = new Page<>(safePageNum, safePageSize);
        Page<UserReadingHistory> result = historyRepository.selectPage(
                page,
                new LambdaQueryWrapper<UserReadingHistory>()
                        .eq(UserReadingHistory::getUserId, userId)
                        .ge(UserReadingHistory::getLastReadAt, cutoffTime())
                        .orderByDesc(UserReadingHistory::getLastReadAt)
        );

        return new PageResult<>(result.getTotal(), safePageNum, safePageSize, toHistoryDTOs(result.getRecords()));
    }

    private List<ReadingHistoryDTO> buildHistory(Long userId, int limit) {
        cleanupExpired(userId);
        List<UserReadingHistory> histories = historyRepository.selectList(
                new LambdaQueryWrapper<UserReadingHistory>()
                        .eq(UserReadingHistory::getUserId, userId)
                        .ge(UserReadingHistory::getLastReadAt, cutoffTime())
                        .orderByDesc(UserReadingHistory::getLastReadAt)
                        .last("LIMIT " + limit)
        );
        if (histories.isEmpty()) {
            return Collections.emptyList();
        }

        return toHistoryDTOs(histories);
    }

    private List<ReadingHistoryDTO> toHistoryDTOs(List<UserReadingHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> articleIds = histories.stream()
                .map(UserReadingHistory::getArticleId)
                .distinct()
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Article> articleMap = articleRepository.selectList(
                new LambdaQueryWrapper<Article>()
                        .in(Article::getId, articleIds)
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
        ).stream().collect(Collectors.toMap(Article::getId, Function.identity()));

        return histories.stream()
                .map(history -> {
                    Article article = articleMap.get(history.getArticleId());
                    if (article == null) {
                        return null;
                    }
                    ReadingHistoryDTO dto = new ReadingHistoryDTO();
                    dto.setArticleId(article.getId());
                    dto.setArticleTitle(article.getTitle());
                    dto.setArticleSummary(article.getSummary());
                    dto.setArticleCoverImage(article.getCoverImage());
                    dto.setLastAnchor(history.getLastAnchor());
                    dto.setLastReadAt(history.getLastReadAt());
                    return dto;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String normalizeAnchor(String lastAnchor) {
        if (lastAnchor == null || lastAnchor.isBlank()) {
            return "";
        }
        String anchor = lastAnchor.trim();
        return anchor.startsWith("#") ? anchor.substring(1) : anchor;
    }

    private void cleanupExpired(Long userId) {
        historyRepository.delete(
                new LambdaQueryWrapper<UserReadingHistory>()
                        .eq(UserReadingHistory::getUserId, userId)
                        .lt(UserReadingHistory::getLastReadAt, cutoffTime())
        );
    }

    private LocalDateTime cutoffTime() {
        return LocalDateTime.now().minusDays(RETENTION_DAYS);
    }
}