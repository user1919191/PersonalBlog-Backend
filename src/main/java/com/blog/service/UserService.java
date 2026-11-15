package main.java.com.blog.service;

import com.blog.dto.UpdateProfileRequest;
import com.blog.dto.UserProfileDTO;
import com.blog.entity.User;
import com.blog.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfileDTO getProfile(Long userId) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        return toDTO(user);
    }

    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.selectById(userId);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        user.setDisplayName(request.getDisplayName());
        userRepository.updateById(user);
        return toDTO(user);
    }

    private UserProfileDTO toDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername());
        dto.setAvatar(user.getAvatar());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
