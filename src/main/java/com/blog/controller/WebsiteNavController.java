package main.java.com.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.PageResult;
import com.blog.dto.Result;
import com.blog.entity.WebsiteNav;
import com.blog.repository.WebsiteNavRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/navs")
public class WebsiteNavController {

    private final WebsiteNavRepository websiteNavRepository;

    public WebsiteNavController(WebsiteNavRepository websiteNavRepository) {
        this.websiteNavRepository = websiteNavRepository;
    }

    @GetMapping
    public Result<PageResult<WebsiteNav>> getNavs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "25") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        LambdaQueryWrapper<WebsiteNav> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) {
            wrapper.eq(WebsiteNav::getCategory, category);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(WebsiteNav::getName, keyword)
                    .or()
                    .like(WebsiteNav::getDescription, keyword));
        }
        wrapper.orderByAsc(WebsiteNav::getSortOrder);

        Page<WebsiteNav> page = new Page<>(pageNum, pageSize);
        page.setSearchCount(true);
        Page<WebsiteNav> result = websiteNavRepository.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getTotal(), pageNum, pageSize, result.getRecords()));
    }
}
