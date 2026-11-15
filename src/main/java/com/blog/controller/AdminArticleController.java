package main.java.com.blog.controller;

import com.blog.dto.ArticleRequest;
import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/articles")
public class AdminArticleController {

    private final ArticleService articleService;

    public AdminArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public Result<PageResult<Article>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "15") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(articleService.adminGetArticles(pageNum, pageSize, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Article> getArticleDetail(@PathVariable Long id) {
        return Result.success(articleService.getAdminArticleById(id));
    }

    @PostMapping
    public Result<Article> createArticle(@Valid @RequestBody ArticleRequest request) {
        Article article = articleService.createArticle(request);
        return Result.success("发布成功", article);
    }

    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        Article article = articleService.updateArticle(id, request);
        return Result.success("更新成功", article);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success("删除成功", null);
    }
}

