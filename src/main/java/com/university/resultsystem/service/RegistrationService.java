package com.university.resultsystem.service;

import com.university.resultsystem.model.AcademicSession;
import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.CourseRegistration;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.repository.AcademicSessionRepository;
import com.university.resultsystem.repository.CourseRegistrationRepository;
import com.university.resultsystem.repository.CourseRepository;
import com.university.resultsystem.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegistrationService {

    private final CourseRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AcademicSessionRepository sessionRepository;

    public RegistrationService(CourseRegistrationRepository registrationRepository, StudentRepository studentRepository,
            CourseRepository courseRepository, AcademicSessionRepository sessionRepository) {
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public CourseRegistration registerStudent(UUID studentId, UUID courseId, UUID sessionId) {
        if (registrationRepository.findByStudentIdAndCourseIdAndSessionId(studentId, courseId, sessionId).isPresent()) {
            throw new RuntimeException("Student already registered for this course in this session");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        AcademicSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        CourseRegistration registration = new CourseRegistration();
        registration.setStudent(student);
        registration.setCourse(course);
        registration.setSession(session);
        registration.setSemester(course.getSemester()); // Default to course semester

        return registrationRepository.save(registration);
    }
}
