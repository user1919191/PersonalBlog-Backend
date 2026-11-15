package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.blog.dto.ArticleSeriesDTO;
import com.blog.entity.Article;
import com.blog.entity.ArticleSeries;
import com.blog.repository.ArticleRepository;
import com.blog.repository.ArticleSeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleSeriesService {

    private static final String VISIBILITY_PUBLIC = "PUBLIC";

    private final ArticleSeriesRepository seriesRepository;
    private final ArticleRepository articleRepository;

    public ArticleSeriesService(ArticleSeriesRepository seriesRepository,
                                ArticleRepository articleRepository) {
        this.seriesRepository = seriesRepository;
        this.articleRepository = articleRepository;
    }

    public List<ArticleSeriesDTO> getAllSeries() {
        return seriesRepository.selectList(
                new LambdaQueryWrapper<ArticleSeries>().orderByAsc(ArticleSeries::getSortOrder)
        ).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ArticleSeriesDTO getSeriesById(Long id) {
        ArticleSeries series = seriesRepository.selectById(id);
        if (series == null) {
            throw new IllegalArgumentException("专栏不存在");
        }

        ArticleSeriesDTO dto = toDTO(series);
        List<Article> articles = articleRepository.selectList(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getSeriesId, id)
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
                        .orderByAsc(Article::getSeriesOrder)
        );
        dto.setArticles(articles.stream().map(article -> {
            ArticleSeriesDTO.SeriesArticleItem item = new ArticleSeriesDTO.SeriesArticleItem();
            item.setId(article.getId());
            item.setTitle(article.getTitle());
            item.setSeriesOrder(article.getSeriesOrder());
            item.setCreateTime(article.getCreateTime());
            return item;
        }).collect(Collectors.toList()));
        return dto;
    }

    public ArticleSeriesDTO.SeriesNavigation getSeriesNavigation(Long articleId, Long seriesId, Integer seriesOrder) {
        ArticleSeries series = seriesRepository.selectById(seriesId);

        Article prev = articleRepository.selectOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getSeriesId, seriesId)
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
                        .lt(Article::getSeriesOrder, seriesOrder)
                        .orderByDesc(Article::getSeriesOrder)
                        .last("LIMIT 1")
        );

        Article next = articleRepository.selectOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getSeriesId, seriesId)
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
                        .gt(Article::getSeriesOrder, seriesOrder)
                        .orderByAsc(Article::getSeriesOrder)
                        .last("LIMIT 1")
        );

        long totalCount = articleRepository.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getSeriesId, seriesId)
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
        );

        ArticleSeriesDTO.SeriesNavigation nav = new ArticleSeriesDTO.SeriesNavigation();
        nav.setSeriesId(seriesId);
        nav.setSeriesName(series != null ? series.getName() : "");
        nav.setCurrentOrder(seriesOrder);
        nav.setTotalCount((int) totalCount);
        if (prev != null) {
            nav.setPrevId(prev.getId());
            nav.setPrevTitle(prev.getTitle());
        }
        if (next != null) {
            nav.setNextId(next.getId());
            nav.setNextTitle(next.getTitle());
        }
        return nav;
    }

    @Transactional
    public ArticleSeries createSeries(ArticleSeries series) {
        validateSeries(series);
        normalizeSeries(series);
        seriesRepository.insert(series);
        return series;
    }

    @Transactional
    public ArticleSeries updateSeries(Long id, ArticleSeries update) {
        ArticleSeries existing = seriesRepository.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("专栏不存在");
        }
        validateSeries(update);
        normalizeSeries(update);
        update.setId(id);
        seriesRepository.updateById(update);
        return update;
    }

    @Transactional
    public void deleteSeries(Long id) {
        articleRepository.update(null,
                new LambdaUpdateWrapper<Article>()
                        .eq(Article::getSeriesId, id)
                        .set(Article::getSeriesId, null)
                        .set(Article::getSeriesOrder, 0)
        );
        seriesRepository.deleteById(id);
    }

    private ArticleSeriesDTO toDTO(ArticleSeries series) {
        ArticleSeriesDTO dto = new ArticleSeriesDTO();
        dto.setId(series.getId());
        dto.setName(series.getName());
        dto.setDescription(series.getDescription());
        dto.setCoverImage(series.getCoverImage());
        dto.setSortOrder(series.getSortOrder());
        Long count = articleRepository.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getSeriesId, series.getId())
                        .eq(Article::getStatus, 1)
                        .eq(Article::getVisibility, VISIBILITY_PUBLIC)
        );
        dto.setArticleCount(count != null ? count.intValue() : 0);
        dto.setCreateTime(series.getCreateTime());
        return dto;
    }

    private void validateSeries(ArticleSeries series) {
        if (series == null || series.getName() == null || series.getName().isBlank()) {
            throw new IllegalArgumentException("专栏名称不能为空");
        }
    }

    private void normalizeSeries(ArticleSeries series) {
        series.setName(series.getName().trim());
        series.setDescription(series.getDescription() == null ? "" : series.getDescription().trim());
        series.setCoverImage(series.getCoverImage() == null ? "" : series.getCoverImage().trim());
        series.setSortOrder(series.getSortOrder() == null ? 0 : series.getSortOrder());
    }
}