package com.university.resultsystem.service;

import com.university.resultsystem.dto.CourseDto;
import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.Lecturer;
import com.university.resultsystem.repository.CourseRepository;
import com.university.resultsystem.repository.LecturerRepository;
import com.university.resultsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final LecturerRepository lecturerRepository;

    public CourseService(CourseRepository courseRepository, LecturerRepository lecturerRepository) {
        this.courseRepository = courseRepository;
        this.lecturerRepository = lecturerRepository;
    }

    @Transactional
    public Course createCourse(CourseDto dto) {
        Course course = new Course();
        course.setCode(dto.getCode());
        course.setTitle(dto.getTitle());
        course.setUnits(dto.getUnits());
        course.setSemester(dto.getSemester());
        course.setLevel(dto.getLevel());
        course.setDepartment(dto.getDepartment());

        if (dto.getLecturerId() != null) {
            UUID lecturerId = java.util.Objects.requireNonNull(dto.getLecturerId(), "Lecturer ID cannot be null");
            Lecturer lecturer = lecturerRepository.findById(lecturerId)
                    .orElseThrow(() -> new RuntimeException("Lecturer not found"));
            course.getLecturers().add(lecturer);
        }

        return courseRepository.save(course);
    }

    @Transactional
    public Course assignLecturers(UUID courseId, List<UUID> lecturerIds) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        List<Lecturer> lecturers = lecturerRepository.findAllById(lecturerIds);
        course.getLecturers().clear();
        course.getLecturers().addAll(lecturers);

        return courseRepository.save(course);
    }

    public boolean isLecturerAssignedToCourse(UUID courseId, String username) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return course.getLecturers().stream()
                .anyMatch(l -> l.getUser().getUsername().equals(username));
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByLecturerUsername(String username) {
        return courseRepository.findByLecturersUserUsername(username);
    }
}
