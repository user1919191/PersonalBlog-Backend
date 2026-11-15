package main.java.com.blog.controller;

import com.blog.dto.ArticleSeriesDTO;
import com.blog.dto.Result;
import com.blog.entity.ArticleSeries;
import com.blog.service.ArticleSeriesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/series")
public class ArticleSeriesController {

    private final ArticleSeriesService seriesService;

    public ArticleSeriesController(ArticleSeriesService seriesService) {
        this.seriesService = seriesService;
    }

    /** 获取所有专栏（公开） */
    @GetMapping
    public Result<List<ArticleSeriesDTO>> getAllSeries() {
        return Result.success(seriesService.getAllSeries());
    }

    /** 获取专栏详情及文章列表（公开） */
    @GetMapping("/{id}")
    public Result<ArticleSeriesDTO> getSeriesById(@PathVariable Long id) {
        return Result.success(seriesService.getSeriesById(id));
    }

    /** 创建专栏（仅管理员） */
    @PostMapping("/admin")
    public Result<ArticleSeries> createSeries(@RequestBody ArticleSeries series) {
        return Result.success(seriesService.createSeries(series));
    }

    /** 更新专栏（仅管理员） */
    @PutMapping("/admin/{id}")
    public Result<ArticleSeries> updateSeries(@PathVariable Long id, @RequestBody ArticleSeries series) {
        return Result.success(seriesService.updateSeries(id, series));
    }

    /** 删除专栏（仅管理员） */
    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteSeries(@PathVariable Long id) {
        seriesService.deleteSeries(id);
        return Result.success("删除成功", null);
    }
}
