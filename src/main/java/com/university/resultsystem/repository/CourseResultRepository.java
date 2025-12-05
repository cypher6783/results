package com.university.resultsystem.repository;

import com.university.resultsystem.model.CourseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseResultRepository extends JpaRepository<CourseResult, UUID> {
    List<CourseResult> findByResultId(UUID resultId);
}
