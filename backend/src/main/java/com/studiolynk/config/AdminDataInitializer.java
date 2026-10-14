package com.studiolynk.config;

import com.studiolynk.model.entity.User;
import com.studiolynk.model.enums.UserRole;
import com.studiolynk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default administrator account if one does not exist (ADM-002).
 * Admin credentials: admin@studiolynk.com / Admin@2026!
 */
@Component
public class AdminDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminDataInitializer.class);
    private static final String DEFAULT_ADMIN_EMAIL = "admin@studiolynk.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@2026!";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(DEFAULT_ADMIN_EMAIL)) {
            User admin = new User(
                    DEFAULT_ADMIN_EMAIL,
                    passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                    UserRole.ADMIN
            );
            admin.setOnboardingCompleted(true);
            userRepository.save(admin);
            log.info("Initialized default admin account: {}", DEFAULT_ADMIN_EMAIL);
        } else {
            log.info("Admin account already exists: {}", DEFAULT_ADMIN_EMAIL);
        }
    }
}
