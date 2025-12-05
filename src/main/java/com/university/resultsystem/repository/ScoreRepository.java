package com.university.resultsystem.repository;

import com.university.resultsystem.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoreRepository extends JpaRepository<Score, UUID> {
    Optional<Score> findByRegistrationId(UUID registrationId);

    Optional<Score> findByRegistration(com.university.resultsystem.model.CourseRegistration registration);
}
