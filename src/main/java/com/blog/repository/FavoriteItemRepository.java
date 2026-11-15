package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.FavoriteItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteItemRepository extends BaseMapper<FavoriteItem> {
}
