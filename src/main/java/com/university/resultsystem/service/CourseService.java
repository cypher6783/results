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
    private final com.university.resultsystem.repository.CourseRegistrationRepository registrationRepository;
    private final com.university.resultsystem.repository.ScoreRepository scoreRepository;
    private final com.university.resultsystem.repository.CourseResultRepository courseResultRepository;

    public CourseService(CourseRepository courseRepository, LecturerRepository lecturerRepository,
            com.university.resultsystem.repository.CourseRegistrationRepository registrationRepository,
            com.university.resultsystem.repository.ScoreRepository scoreRepository,
            com.university.resultsystem.repository.CourseResultRepository courseResultRepository) {
        this.courseRepository = courseRepository;
        this.lecturerRepository = lecturerRepository;
        this.registrationRepository = registrationRepository;
        this.scoreRepository = scoreRepository;
        this.courseResultRepository = courseResultRepository;
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
            Lecturer lecturer = lecturerRepository.findById(dto.getLecturerId())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found with ID: " + dto.getLecturerId()));
            course.getLecturers().add(lecturer);
        } else if (dto.getLecturerStaffId() != null && !dto.getLecturerStaffId().isBlank()) {
            Lecturer lecturer = lecturerRepository.findByStaffId(dto.getLecturerStaffId().trim())
                    .orElseThrow(() -> new RuntimeException(
                            "Lecturer not found with Staff ID: " + dto.getLecturerStaffId()));
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

    @Transactional
    public Course updateCourse(UUID id, CourseDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setCode(dto.getCode());
        course.setTitle(dto.getTitle());
        course.setUnits(dto.getUnits());
        course.setSemester(dto.getSemester());
        course.setLevel(dto.getLevel());
        course.setDepartment(dto.getDepartment());

        // Update lecturer if provided
        if (dto.getLecturerId() != null) {
            Lecturer lecturer = lecturerRepository.findById(dto.getLecturerId())
                    .orElseThrow(() -> new RuntimeException("Lecturer not found with ID: " + dto.getLecturerId()));
            course.getLecturers().clear();
            course.getLecturers().add(lecturer);
        } else if (dto.getLecturerStaffId() != null && !dto.getLecturerStaffId().isBlank()) {
            Lecturer lecturer = lecturerRepository.findByStaffId(dto.getLecturerStaffId().trim())
                    .orElseThrow(() -> new RuntimeException(
                            "Lecturer not found with Staff ID: " + dto.getLecturerStaffId()));
            course.getLecturers().clear();
            course.getLecturers().add(lecturer);
        }

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found");
        }

        // 1. Delete scores associated with registrations for this course
        scoreRepository.deleteByRegistrationCourseId(id);

        // 2. Delete course registrations
        registrationRepository.deleteByCourseId(id);

        // 3. Delete course result snapshots
        courseResultRepository.deleteByCourseId(id);

        // 4. Finally delete the course
        courseRepository.deleteById(id);
    }
}
