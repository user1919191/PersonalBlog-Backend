package main.java.com.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.AdminUserDTO;
import com.blog.dto.Result;
import com.blog.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /** 分页查询用户列表 */
    @GetMapping
    public Result<Page<AdminUserDTO>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminUserService.listUsers(page, size, keyword));
    }

    /** 封禁 / 解封用户（封禁时立即清除所有 Session） */
    @PatchMapping("/{userId}/status")
    public Result<Void> updateStatus(@PathVariable Long userId,
                                     @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            return Result.error(400, "status 只能为 0（封禁）或 1（正常）");
        }
        adminUserService.updateStatus(userId, status);
        return Result.success(status == 0 ? "封禁成功，用户已下线" : "已解除封禁", null);
    }
}
