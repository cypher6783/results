package com.university.resultsystem.config;

import com.university.resultsystem.model.Role;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String username = "admin";
            String defaultPassword = "admin123";
            boolean forceReset = "true".equalsIgnoreCase(System.getenv("ADMIN_PASSWORD_RESET"));

            System.out.println("Checking data initialization for user: " + username);

            Optional<User> existingAdmin = userRepository.findByUsername(username);

            if (existingAdmin.isEmpty()) {
                System.out.println("No admin user found. Creating default admin...");
                User admin = new User();
                admin.setUsername(username);
                admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
                admin.setFullName("System Administrator");
                admin.setRole(Role.ADMIN);
                admin.setEmail("admin@university.com");
                userRepository.save(admin);
                System.out.println("Default Admin User Created Successfully: admin / " + defaultPassword);
            } else if (forceReset) {
                System.out.println("ADMIN_PASSWORD_RESET=true detected. Forcing password reset for admin user...");
                User admin = existingAdmin.get();
                admin.setPasswordHash(passwordEncoder.encode(defaultPassword));
                userRepository.save(admin);
                System.out.println("Admin password has been reset to: " + defaultPassword);
            } else {
                User admin = existingAdmin.get();
                System.out.println("Admin user already exists (ID: " + admin.getId() + ").");
                System.out.println("If login fails, try 'admin123' or set ADMIN_PASSWORD_RESET=true env variable.");
            }
        };
    }
}
