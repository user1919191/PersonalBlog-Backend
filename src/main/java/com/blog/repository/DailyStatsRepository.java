package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.DailyStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DailyStatsRepository extends BaseMapper<DailyStats> {
}
