package com.university.resultsystem.repository;

import com.university.resultsystem.model.PasswordResetToken;
import com.university.resultsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByExpiryDateBefore(LocalDateTime now);
}
