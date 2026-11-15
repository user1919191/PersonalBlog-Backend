package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.StatsDTO;
import com.blog.entity.Article;
import com.blog.entity.DailyStats;
import com.blog.repository.ArticleRepository;
import com.blog.repository.DailyStatsRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VisitLogService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("MM-dd");
    private static final long SHORT_TTL_DAYS = 3;
    private static final long ARTICLE_RANK_TTL_DAYS = 30;
    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    private final RedisTemplate<String, Object> redisTemplate;
    private final DailyStatsRepository dailyStatsRepository;
    private final ArticleRepository articleRepository;

    public VisitLogService(RedisTemplate<String, Object> redisTemplate,
                           DailyStatsRepository dailyStatsRepository,
                           ArticleRepository articleRepository) {
        this.redisTemplate = redisTemplate;
        this.dailyStatsRepository = dailyStatsRepository;
        this.articleRepository = articleRepository;
    }

    @Async
    public void recordVisit(String visitorId) {
        String today = todayKey();
        String pvKey = buildPvKey(today);
        String uvKey = buildUvKey(today);

        redisTemplate.opsForValue().increment(pvKey);
        redisTemplate.expire(pvKey, SHORT_TTL_DAYS, TimeUnit.DAYS);

        if (visitorId != null && !visitorId.isBlank()) {
            redisTemplate.opsForHyperLogLog().add(uvKey, visitorId);
            redisTemplate.expire(uvKey, SHORT_TTL_DAYS, TimeUnit.DAYS);
        }
    }

    @Async
    public void recordArticleVisit(Long articleId) {
        if (articleId == null) {
            return;
        }
        String today = todayKey();
        String articlePvKey = buildArticlePvKey(today);
        String articleRankKey = buildArticleRankKey(today);

        redisTemplate.opsForValue().increment(articlePvKey);
        redisTemplate.expire(articlePvKey, ARTICLE_RANK_TTL_DAYS, TimeUnit.DAYS);

        redisTemplate.opsForZSet().incrementScore(articleRankKey, articleId.toString(), 1D);
        redisTemplate.expire(articleRankKey, ARTICLE_RANK_TTL_DAYS, TimeUnit.DAYS);
    }

    @Scheduled(cron = "0 5 1 * * *")
    public void persistYesterdayStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DATE_FMT);

        long pv = getRedisPV(dateStr);
        long uv = getRedisUV(dateStr);

        LambdaQueryWrapper<DailyStats> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DailyStats::getStatDate, yesterday);
        if (dailyStatsRepository.selectCount(wrapper) == 0) {
            DailyStats stats = new DailyStats();
            stats.setStatDate(yesterday);
            stats.setPv(pv);
            stats.setUv(uv);
            dailyStatsRepository.insert(stats);
        }

        redisTemplate.delete(buildPvKey(dateStr));
        redisTemplate.delete(buildUvKey(dateStr));
    }

    public List<StatsDTO> getDailyStats(int days) {
        List<StatsDTO> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long pv;
            long uv;

            if (i == 0) {
                pv = getRedisPV(date.format(DATE_FMT));
                uv = getRedisUV(date.format(DATE_FMT));
            } else {
                LambdaQueryWrapper<DailyStats> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DailyStats::getStatDate, date);
                DailyStats record = dailyStatsRepository.selectOne(wrapper);
                pv = record != null ? record.getPv() : 0L;
                uv = record != null ? record.getUv() : 0L;
            }

            result.add(new StatsDTO(date.format(DISPLAY_FMT), pv, uv));
        }
        return result;
    }

    public long getTodayPV() {
        return getRedisPV(todayKey());
    }

    public long getTodayUV() {
        return getRedisUV(todayKey());
    }

    public long getTodayArticlePV() {
        Object value = redisTemplate.opsForValue().get(buildArticlePvKey(todayKey()));
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    public List<Map<String, Object>> getTodayTopArticles(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);
        String rankKey = buildArticleRankKey(todayKey());
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(rankKey, 0, safeLimit - 1);
        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }

        List<Long> articleIds = tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(value -> value != null)
                .map(Object::toString)
                .map(Long::valueOf)
                .collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Article> articleMap = articleRepository.selectList(
                new LambdaQueryWrapper<Article>()
                        .in(Article::getId, articleIds)
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
        ).stream().collect(Collectors.toMap(Article::getId, article -> article, (left, right) -> left, HashMap::new));

        List<Map<String, Object>> result = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple.getValue() == null) {
                continue;
            }
            Long articleId = Long.valueOf(tuple.getValue().toString());
            Article article = articleMap.get(articleId);
            if (article == null) {
                continue;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getTitle());
            item.put("todayViewCount", tuple.getScore() == null ? 0L : tuple.getScore().longValue());
            item.put("totalViewCount", article.getViewCount() == null ? 0 : article.getViewCount());
            item.put("commentCount", article.getCommentCount() == null ? 0 : article.getCommentCount());
            item.put("likeCount", article.getLikeCount() == null ? 0 : article.getLikeCount());
            result.add(item);
        }
        return result;
    }

    private long getRedisPV(String dateStr) {
        Object val = redisTemplate.opsForValue().get(buildPvKey(dateStr));
        return val != null ? Long.parseLong(val.toString()) : 0L;
    }

    private long getRedisUV(String dateStr) {
        Long size = redisTemplate.opsForHyperLogLog().size(buildUvKey(dateStr));
        return size != null ? size : 0L;
    }

    private String todayKey() {
        return LocalDate.now().format(DATE_FMT);
    }

    private String buildPvKey(String dateStr) {
        return "stats:pv:" + dateStr;
    }

    private String buildUvKey(String dateStr) {
        return "stats:uv:" + dateStr;
    }

    private String buildArticlePvKey(String dateStr) {
        return "stats:article:pv:" + dateStr;
    }

    private String buildArticleRankKey(String dateStr) {
        return "stats:article:rank:" + dateStr;
    }
}