package main.java.com.blog.controller;

import com.blog.dto.Result;
import com.blog.dto.UpdateProfileRequest;
import com.blog.dto.UserProfileDTO;
import com.blog.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public Result<UserProfileDTO> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public Result<UserProfileDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest req,
                                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("更新成功", userService.updateProfile(userId, req));
    }
}
