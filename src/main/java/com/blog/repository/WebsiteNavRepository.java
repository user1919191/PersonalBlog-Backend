package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.WebsiteNav;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WebsiteNavRepository extends BaseMapper<WebsiteNav> {
}
