package com.university.resultsystem.config;

import com.university.resultsystem.model.Role;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("admin123"));
                admin.setFullName("System Administrator");
                admin.setRole(Role.ADMIN);
                admin.setEmail("admin@university.com");
                userRepository.save(admin);
                System.out.println("Default Admin User Created: admin / admin123");
            }
        };
    }
}
