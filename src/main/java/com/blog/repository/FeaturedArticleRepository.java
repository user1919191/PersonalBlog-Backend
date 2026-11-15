package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.FeaturedArticle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeaturedArticleRepository extends BaseMapper<FeaturedArticle> {
}
