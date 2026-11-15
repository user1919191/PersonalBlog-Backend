package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.VisitLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VisitLogRepository extends BaseMapper<VisitLog> {
}
