package main.java.com.blog.controller;

import com.blog.dto.Result;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.getAllCategories());
    }

    @PostMapping
    public Result<Category> create(@RequestBody CategoryRequest req) {
        Category category = categoryService.createCategory(req.getName(), req.getDescription(), req.getSortOrder());
        return Result.success("创建成功", category);
    }

    @PutMapping("/{id}")
    public Result<Category> update(@PathVariable Long id, @RequestBody CategoryRequest req) {
        Category category = categoryService.updateCategory(id, req.getName(), req.getDescription(), req.getSortOrder());
        return Result.success("更新成功", category);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功", null);
    }

    @Data
    public static class CategoryRequest {
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 50, message = "名称最多50字")
        private String name;

        @Size(max = 200, message = "描述最多200字")
        private String description;

        private Integer sortOrder = 0;
    }
}
