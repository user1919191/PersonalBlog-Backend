package main.java.com.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.dto.AdminUserDTO;
import com.blog.entity.User;
import com.blog.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AdminUserService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    /** 分页查询用户列表，支持关键字搜索 */
    public Page<AdminUserDTO> listUsers(int page, int size, String keyword) {
        Page<User> pageReq = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getUsername, keyword)
                   .or().like(User::getDisplayName, keyword)
                   .or().like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userRepository.selectPage(pageReq, wrapper);

        Page<AdminUserDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    /**
     * 更新用户状态（封禁/解封）
     * 封禁时立即清除该用户所有 Redis Session，踢出所有设备
     */
    public void updateStatus(Long userId, Integer status) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        if ("ADMIN".equals(user.getRole())) throw new IllegalArgumentException("不可操作管理员账户");

        user.setStatus(status);
        userRepository.updateById(user);

        // 封禁时立即踢出所有在线 Session
        if (status == 0) {
            sessionService.invalidateAllSessions(userId);
        }
    }

    private AdminUserDTO toDTO(User user) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setOauthProvider(user.getOauthProvider());
        dto.setGiteeId(user.getGiteeId());
        dto.setCreateTime(user.getCreateTime());
        dto.setSessionCount(sessionService.getSessionCount(user.getId()));
        return dto;
    }
}
