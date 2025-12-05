package com.university.resultsystem.repository;

import com.university.resultsystem.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResultRepository extends JpaRepository<Result, UUID> {
    List<Result> findByStudentId(UUID studentId);

    Optional<Result> findByStudentIdAndSessionIdAndSemester(UUID studentId, UUID sessionId, Integer semester);

    List<Result> findBySessionIdAndSemester(UUID sessionId, Integer semester);
}
