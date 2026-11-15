package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Category;
import com.blog.repository.CategoryRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CategoryService {

    private static final String CACHE_KEY = "categories:all";

    private final CategoryRepository categoryRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public CategoryService(CategoryRepository categoryRepository, RedisTemplate<String, Object> redisTemplate) {
        this.categoryRepository = categoryRepository;
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<Category> getAllCategories() {
        List<Category> categories = (List<Category>) redisTemplate.opsForValue().get(CACHE_KEY);
        if (categories == null) {
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Category::getSortOrder);
            categories = categoryRepository.selectList(wrapper);
            redisTemplate.opsForValue().set(CACHE_KEY, categories, 1, TimeUnit.DAYS);
        }
        return categories;
    }

    @Transactional
    public Category createCategory(String name, String description, Integer sortOrder) {
        LambdaQueryWrapper<Category> check = new LambdaQueryWrapper<>();
        check.eq(Category::getName, name);
        if (categoryRepository.selectCount(check) > 0) {
            throw new IllegalArgumentException("分类名称已存在");
        }
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSortOrder(sortOrder != null ? sortOrder : 0);
        categoryRepository.insert(category);
        redisTemplate.delete(CACHE_KEY);
        return category;
    }

    @Transactional
    public Category updateCategory(Long id, String name, String description, Integer sortOrder) {
        Category category = categoryRepository.selectById(id);
        if (category == null) throw new IllegalArgumentException("分类不存在");

        LambdaQueryWrapper<Category> check = new LambdaQueryWrapper<>();
        check.eq(Category::getName, name).ne(Category::getId, id);
        if (categoryRepository.selectCount(check) > 0) {
            throw new IllegalArgumentException("分类名称已存在");
        }
        category.setName(name);
        category.setDescription(description);
        category.setSortOrder(sortOrder != null ? sortOrder : category.getSortOrder());
        categoryRepository.updateById(category);
        redisTemplate.delete(CACHE_KEY);
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.selectById(id);
        if (category == null) throw new IllegalArgumentException("分类不存在");
        categoryRepository.deleteById(id);
        redisTemplate.delete(CACHE_KEY);
    }
}
