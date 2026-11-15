package main.java.com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.dto.Result;
import com.blog.dto.StatsDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.ForumPost;
import com.blog.entity.InterviewComment;
import com.blog.entity.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.ArticleSeriesRepository;
import com.blog.repository.CategoryRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.FeaturedArticleRepository;
import com.blog.repository.ForumPostRepository;
import com.blog.repository.InterviewCommentRepository;
import com.blog.repository.InterviewRepository;
import com.blog.repository.TagRepository;
import com.blog.repository.UserRepository;
import com.blog.repository.WebsiteNavRepository;
import com.blog.service.VisitLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    private final VisitLogService visitLogService;
    private final ArticleRepository articleRepository;
    private final FeaturedArticleRepository featuredArticleRepository;
    private final WebsiteNavRepository websiteNavRepository;
    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final InterviewCommentRepository interviewCommentRepository;
    private final ForumPostRepository forumPostRepository;
    private final ArticleSeriesRepository articleSeriesRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public AdminStatsController(VisitLogService visitLogService,
                                ArticleRepository articleRepository,
                                FeaturedArticleRepository featuredArticleRepository,
                                WebsiteNavRepository websiteNavRepository,
                                InterviewRepository interviewRepository,
                                UserRepository userRepository,
                                CommentRepository commentRepository,
                                InterviewCommentRepository interviewCommentRepository,
                                ForumPostRepository forumPostRepository,
                                ArticleSeriesRepository articleSeriesRepository,
                                CategoryRepository categoryRepository,
                                TagRepository tagRepository) {
        this.visitLogService = visitLogService;
        this.articleRepository = articleRepository;
        this.featuredArticleRepository = featuredArticleRepository;
        this.websiteNavRepository = websiteNavRepository;
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.interviewCommentRepository = interviewCommentRepository;
        this.forumPostRepository = forumPostRepository;
        this.articleSeriesRepository = articleSeriesRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();

        long publishedArticles = countArticlesByStatus(1);
        long draftArticles = countArticlesByStatus(0);
        long enabledUsers = userRepository.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long totalComments = commentRepository.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 1))
                + interviewCommentRepository.selectCount(new LambdaQueryWrapper<InterviewComment>().eq(InterviewComment::getStatus, 1));
        long totalForumPosts = forumPostRepository.selectCount(new LambdaQueryWrapper<ForumPost>().eq(ForumPost::getStatus, 1));

        List<StatsDTO> weeklyStats = visitLogService.getDailyStats(7);
        long weeklyPv = weeklyStats.stream().mapToLong(StatsDTO::getPv).sum();
        long weeklyUv = weeklyStats.stream().mapToLong(StatsDTO::getUv).sum();

        data.put("todayPV", visitLogService.getTodayPV());
        data.put("todayUV", visitLogService.getTodayUV());
        data.put("todayArticlePV", visitLogService.getTodayArticlePV());
        data.put("weeklyPV", weeklyPv);
        data.put("weeklyUV", weeklyUv);

        data.put("totalArticles", articleRepository.selectCount(null));
        data.put("publishedArticles", publishedArticles);
        data.put("draftArticles", draftArticles);
        data.put("totalFeatured", featuredArticleRepository.selectCount(null));
        data.put("totalNavs", websiteNavRepository.selectCount(null));
        data.put("totalInterviews", interviewRepository.selectCount(null));
        data.put("totalUsers", userRepository.selectCount(null));
        data.put("enabledUsers", enabledUsers);
        data.put("totalComments", totalComments);
        data.put("totalForumPosts", totalForumPosts);
        data.put("totalSeries", articleSeriesRepository.selectCount(null));
        data.put("totalCategories", categoryRepository.selectCount(null));
        data.put("totalTags", tagRepository.selectCount(null));
        return Result.success(data);
    }

    @GetMapping("/daily")
    public Result<List<StatsDTO>> daily(@RequestParam(defaultValue = "7") int days) {
        return Result.success(visitLogService.getDailyStats(days));
    }

    @GetMapping("/top-articles")
    public Result<List<Map<String, Object>>> topArticles(@RequestParam(defaultValue = "10") int limit) {
        List<Map<String, Object>> result = visitLogService.getTodayTopArticles(limit).stream()
                .peek(item -> {
                    String title = String.valueOf(item.get("title"));
                    item.put("title", title.length() > 26 ? title.substring(0, 26) + "..." : title);
                })
                .collect(Collectors.toList());
        return Result.success(result);
    }

    private long countArticlesByStatus(int status) {
        return articleRepository.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getStatus, status));
    }
}
