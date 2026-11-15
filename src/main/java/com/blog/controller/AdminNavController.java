package main.java.com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.WebsiteNav;
import com.blog.repository.WebsiteNavRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/navs")
public class AdminNavController {

    private final WebsiteNavRepository websiteNavRepository;

    public AdminNavController(WebsiteNavRepository websiteNavRepository) {
        this.websiteNavRepository = websiteNavRepository;
    }

    @GetMapping
    public Result<PageResult<WebsiteNav>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<WebsiteNav> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) wrapper.eq(WebsiteNav::getCategory, category);
        if (keyword != null && !keyword.isEmpty())
            wrapper.and(w -> w.like(WebsiteNav::getName, keyword).or().like(WebsiteNav::getDescription, keyword));
        wrapper.orderByAsc(WebsiteNav::getSortOrder);

        Page<WebsiteNav> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(true);
        Page<WebsiteNav> result = websiteNavRepository.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords()));
    }

    @PostMapping
    public Result<WebsiteNav> create(@RequestBody WebsiteNav nav) {
        websiteNavRepository.insert(nav);
        return Result.success("添加成功", nav);
    }

    @PutMapping("/{id}")
    public Result<WebsiteNav> update(@PathVariable Long id, @RequestBody WebsiteNav nav) {
        nav.setId(id);
        websiteNavRepository.updateById(nav);
        return Result.success("更新成功", nav);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        websiteNavRepository.deleteById(id);
        return Result.success("删除成功", null);
    }
}
