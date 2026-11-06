package com.venkatesh.irctc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.venkatesh.irctc.repository.UserRepository;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final String adminUsername;

    public AdminUserInitializer(UserRepository userRepository,
            @Value("${app.admin.username:}") String adminUsername) {
        this.userRepository = userRepository;
        this.adminUsername = adminUsername;
    }

    @Override
    public void run(String... args) {
        if (adminUsername == null || adminUsername.isBlank()) {
            return;
        }

        userRepository.findByUsername(adminUsername.trim()).ifPresent(user -> {
            if (!"ROLE_ADMIN".equals(user.getRole())) {
                user.setRole("ROLE_ADMIN");
                userRepository.save(user);
            }
        });
    }
}
