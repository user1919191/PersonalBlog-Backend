package main.java.com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ForumReply;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ForumReplyRepository extends BaseMapper<ForumReply> {
}
