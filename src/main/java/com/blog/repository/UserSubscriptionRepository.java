package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.UserSubscription;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserSubscriptionRepository extends BaseMapper<UserSubscription> {
}
