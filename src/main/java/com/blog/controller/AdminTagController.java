package main.java.com.blog.controller;

import com.blog.dto.Result;
import com.blog.entity.Tag;
import com.blog.repository.ArticleTagRepository;
import com.blog.repository.TagRepository;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/tags")
public class AdminTagController {

    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    public AdminTagController(TagRepository tagRepository, ArticleTagRepository articleTagRepository) {
        this.tagRepository = tagRepository;
        this.articleTagRepository = articleTagRepository;
    }

    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagRepository.selectList(
                new LambdaQueryWrapper<Tag>().orderByDesc(Tag::getArticleCount).orderByAsc(Tag::getCreateTime)
        ));
    }

    @PostMapping
    public Result<Tag> create(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) return Result.error("标签名不能为空");
        Tag tag = new Tag();
        tag.setName(name.trim());
        tag.setArticleCount(0);
        tagRepository.insert(tag);
        return Result.success(tag);
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Tag tag = tagRepository.selectById(id);
        if (tag == null) return Result.error("标签不存在");
        String name = body.get("name");
        if (name == null || name.isBlank()) return Result.error("标签名不能为空");
        tag.setName(name.trim());
        tagRepository.updateById(tag);
        return Result.success(tag);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagRepository.deleteById(id);
        articleTagRepository.delete(
                new LambdaQueryWrapper<com.blog.entity.ArticleTag>()
                        .eq(com.blog.entity.ArticleTag::getTagId, id)
        );
        return Result.success(null);
    }
}
