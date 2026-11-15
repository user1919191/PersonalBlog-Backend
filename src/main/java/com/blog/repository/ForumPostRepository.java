package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ForumPostRepository extends BaseMapper<ForumPost> {
}
