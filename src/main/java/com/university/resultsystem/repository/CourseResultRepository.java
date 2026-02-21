package com.university.resultsystem.repository;

import com.university.resultsystem.model.CourseResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseResultRepository extends JpaRepository<CourseResult, UUID> {
    List<CourseResult> findByResultId(UUID resultId);

    @Modifying
    @Transactional
    void deleteByResultId(UUID resultId);

    @Modifying
    @Transactional
    void deleteByCourseId(UUID courseId);

    @Modifying
    @Transactional
    void deleteByResult_SessionIdAndResult_Semester(UUID sessionId, Integer semester);
}
