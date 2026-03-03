package com.university.resultsystem.service;

import com.university.resultsystem.dto.ScoreEntryDto;
import com.university.resultsystem.model.AcademicSession;
import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.CourseRegistration;
import com.university.resultsystem.model.Score;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.AcademicSessionRepository;
import com.university.resultsystem.repository.CourseRegistrationRepository;
import com.university.resultsystem.repository.CourseRepository;
import com.university.resultsystem.repository.ScoreRepository;
import com.university.resultsystem.repository.StudentRepository;
import com.university.resultsystem.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScoreService {

    private final ScoreRepository scoreRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final AcademicSessionRepository sessionRepository;

    private final AuditService auditService;

    public ScoreService(ScoreRepository scoreRepository, CourseRegistrationRepository registrationRepository,
            CourseRepository courseRepository, UserRepository userRepository,
            StudentRepository studentRepository, AcademicSessionRepository sessionRepository,
            AuditService auditService) {
        this.scoreRepository = scoreRepository;
        this.registrationRepository = registrationRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Score saveScore(ScoreEntryDto dto, String lecturerUsername) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User lecturer = userRepository.findByUsername(lecturerUsername)
                .orElseThrow(() -> new RuntimeException("Lecturer not found"));

        // Access Control: Check if lecturer is assigned to this course
        if (!lecturer.getRole().name().equals("ADMIN")) {
            boolean isAssigned = course.getLecturers().stream()
                    .anyMatch(l -> l.getUser().getId().equals(lecturer.getId()));
            if (!isAssigned) {
                throw new RuntimeException("Access Denied: You are not assigned to this course.");
            }
        }

        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        AcademicSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        CourseRegistration registration = registrationRepository
                .findByStudentAndCourseAndSession(student, course, session)
                .orElseGet(() -> {
                    // Auto-register if not exists (for simplicity in this phase)
                    CourseRegistration newReg = new CourseRegistration();
                    newReg.setStudent(student);
                    newReg.setCourse(course);
                    newReg.setSession(session);
                    newReg.setSemester(course.getSemester()); // Default to course semester
                    return registrationRepository.save(newReg);
                });

        Score score = scoreRepository.findByRegistration(registration)
                .orElse(new Score());

        String oldValues = "N/A";
        if (score.getId() != null) {
            oldValues = String.format("CA: %.2f, Exam: %.2f", score.getCaScore(), score.getExamScore());
        }

        if (dto.getCaScore() == null || dto.getExamScore() == null) {
            throw new RuntimeException("Both CA and Exam scores are required.");
        }

        score.setRegistration(registration);
        score.setCaScore(dto.getCaScore());
        score.setExamScore(dto.getExamScore());

        double total = dto.getCaScore() + dto.getExamScore();
        score.setTotalScore(total);
        score.setGrade(calculateGrade(total));
        score.setGradePoint(calculateGradePoint(total));

        Score savedScore = scoreRepository.save(score);

        String newValues = String.format("CA: %.2f, Exam: %.2f", savedScore.getCaScore(), savedScore.getExamScore());
        String changeLog = String.format("Old [%s] -> New [%s]", oldValues, newValues);

        auditService.logAction(lecturerUsername, "ENTER_SCORE", "Score", savedScore.getId().toString(),
                changeLog);

        return savedScore;
    }

    private String calculateGrade(double total) {
        if (total >= 70)
            return "A";
        if (total >= 60)
            return "B";
        if (total >= 50)
            return "C";
        if (total >= 45)
            return "D";
        if (total >= 40)
            return "E";
        return "F";
    }

    private double calculateGradePoint(double total) {
        if (total >= 70)
            return 5.0;
        if (total >= 60)
            return 4.0;
        if (total >= 50)
            return 3.0;
        if (total >= 45)
            return 2.0;
        if (total >= 40)
            return 1.0;
        return 0.0;
    }
}
