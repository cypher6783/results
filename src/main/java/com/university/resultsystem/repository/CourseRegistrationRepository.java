package com.university.resultsystem.repository;

import com.university.resultsystem.model.CourseRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, UUID> {
    List<CourseRegistration> findByStudentId(UUID studentId);

    List<CourseRegistration> findByCourseIdAndSessionId(UUID courseId, UUID sessionId);

    Optional<CourseRegistration> findByStudentIdAndCourseIdAndSessionId(UUID studentId, UUID courseId, UUID sessionId);

    Optional<CourseRegistration> findByStudentAndCourseAndSession(com.university.resultsystem.model.Student student,
            com.university.resultsystem.model.Course course, com.university.resultsystem.model.AcademicSession session);
}
