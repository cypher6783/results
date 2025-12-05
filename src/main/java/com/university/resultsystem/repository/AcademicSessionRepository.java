package com.university.resultsystem.repository;

import com.university.resultsystem.model.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AcademicSessionRepository extends JpaRepository<AcademicSession, UUID> {
    Optional<AcademicSession> findByName(String name);
    Optional<AcademicSession> findByIsCurrentTrue();
}
