package com.university.resultsystem.repository;

import com.university.resultsystem.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByCode(String code);

    List<Course> findByDepartment(String department);

    List<Course> findByLevel(Integer level);

    List<Course> findByLecturersUserUsername(String username);
}
