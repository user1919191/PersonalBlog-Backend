package main.java.com.blog.config;

import com.blog.entity.User;
import com.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时确保管理员账户密码是正确的 BCrypt 格式。
 * 账号与初始密码通过配置项注入，避免在代码中硬编码真实凭据。
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${blog.admin.username:admin}")
    private String adminUsername;

    @Value("${blog.admin.password:}")
    private String adminPassword;

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminUsername == null || adminUsername.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            log.info("[AdminInitializer] 未配置管理员初始密码，跳过密码初始化");
            return;
        }
        ensurePassword(adminUsername, adminPassword);
    }

    private void ensurePassword(String username, String expectedPlain) {
        userRepository.findByUsername(username).ifPresent(user -> {
            String pwd = user.getPassword();
            boolean valid = pwd != null && pwd.startsWith("$2a$") && pwd.length() == 60
                    && encoder.matches(expectedPlain, pwd);
            if (!valid) {
                user.setPassword(encoder.encode(expectedPlain));
                userRepository.updateById(user);
                log.info("[AdminInitializer] 已重置管理员 {} 的密码", username);
            }
        });
    }
}
