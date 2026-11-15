package main.java.com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.FeaturedArticle;
import com.blog.repository.FeaturedArticleRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/featured")
public class AdminFeaturedController {

    private final FeaturedArticleRepository featuredArticleRepository;

    public AdminFeaturedController(FeaturedArticleRepository featuredArticleRepository) {
        this.featuredArticleRepository = featuredArticleRepository;
    }

    @GetMapping
    public Result<PageResult<FeaturedArticle>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<FeaturedArticle> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) wrapper.eq(FeaturedArticle::getCategory, category);
        if (keyword != null && !keyword.isEmpty())
            wrapper.and(w -> w.like(FeaturedArticle::getTitle, keyword).or().like(FeaturedArticle::getDescription, keyword));
        wrapper.orderByAsc(FeaturedArticle::getSortOrder);

        Page<FeaturedArticle> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(true);
        Page<FeaturedArticle> result = featuredArticleRepository.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords()));
    }

    @PostMapping
    public Result<FeaturedArticle> create(@RequestBody FeaturedArticle article) {
        featuredArticleRepository.insert(article);
        return Result.success("添加成功", article);
    }

    @PutMapping("/{id}")
    public Result<FeaturedArticle> update(@PathVariable Long id, @RequestBody FeaturedArticle article) {
        article.setId(id);
        featuredArticleRepository.updateById(article);
        return Result.success("更新成功", article);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        featuredArticleRepository.deleteById(id);
        return Result.success("删除成功", null);
    }
}
