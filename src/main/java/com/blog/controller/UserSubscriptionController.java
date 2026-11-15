package main.java.com.blog.controller;

import com.blog.dto.Result;
import com.blog.dto.SubscriptionRequest;
import com.blog.dto.UserSubscriptionDTO;
import com.blog.service.UserSubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/subscriptions")
public class UserSubscriptionController {

    private final UserSubscriptionService userSubscriptionService;

    public UserSubscriptionController(UserSubscriptionService userSubscriptionService) {
        this.userSubscriptionService = userSubscriptionService;
    }

    @GetMapping
    public Result<List<UserSubscriptionDTO>> list(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(userSubscriptionService.getSubscriptions(userId));
    }

    @PostMapping
    public Result<Void> subscribe(@Valid @RequestBody SubscriptionRequest request,
                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userSubscriptionService.subscribe(userId, request.getType(), request.getTargetId());
        return Result.success("订阅成功", null);
    }

    @DeleteMapping
    public Result<Void> unsubscribe(@RequestParam String type,
                                    @RequestParam Long targetId,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        userSubscriptionService.unsubscribe(userId, type, targetId);
        return Result.success("取消订阅成功", null);
    }
}
