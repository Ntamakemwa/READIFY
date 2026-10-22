package com.readify.config;

import com.readify.auth.entity.User;
import com.readify.auth.entity.UserRole;
import com.readify.auth.repository.UserRepository;
import com.readify.common.enums.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User superAdmin = userRepository.findByEmail("superadmin@readify.local")
                .orElseGet(User::new);

        superAdmin.setFullName("System Super Admin");
        superAdmin.setEmail("superadmin@readify.local");
        if (superAdmin.getPassword() == null || superAdmin.getPassword().isBlank()) {
            superAdmin.setPassword(passwordEncoder.encode("Admin@123"));
        }
        superAdmin.setStatus(UserStatus.ACTIVE);
        superAdmin.setEnabled(true);
        superAdmin.setRole(UserRole.ROLE_SUPER_ADMIN);
        userRepository.save(superAdmin);
    }
}
