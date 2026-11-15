package main.java.com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.FeaturedArticle;
import com.blog.repository.FeaturedArticleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/featured")
public class FeaturedArticleController {

    private final FeaturedArticleRepository featuredArticleRepository;

    public FeaturedArticleController(FeaturedArticleRepository featuredArticleRepository) {
        this.featuredArticleRepository = featuredArticleRepository;
    }

    @GetMapping
    public Result<PageResult<FeaturedArticle>> getFeaturedArticles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "30") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<FeaturedArticle> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(FeaturedArticle::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(FeaturedArticle::getTitle, keyword)
                              .or()
                              .like(FeaturedArticle::getDescription, keyword));
        }
        wrapper.orderByAsc(FeaturedArticle::getSortOrder);

        Page<FeaturedArticle> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(true);
        Page<FeaturedArticle> result = featuredArticleRepository.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords()));
    }

    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        List<FeaturedArticle> all = featuredArticleRepository.selectList(
                new LambdaQueryWrapper<FeaturedArticle>()
                        .select(FeaturedArticle::getCategory)
                        .isNotNull(FeaturedArticle::getCategory)
        );
        List<String> categories = all.stream()
                .map(FeaturedArticle::getCategory)
                .filter(c -> !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        return Result.success(categories);
    }
}
