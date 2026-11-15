package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Interview;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewRepository extends BaseMapper<Interview> {
}
