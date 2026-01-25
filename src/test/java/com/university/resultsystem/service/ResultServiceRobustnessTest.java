package com.university.resultsystem.service;

import com.university.resultsystem.model.*;
import com.university.resultsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResultServiceRobustnessTest {

    @Mock
    private ResultRepository resultRepository;
    @Mock
    private ScoreRepository scoreRepository;
    @Mock
    private CourseRegistrationRepository registrationRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private AcademicSessionRepository sessionRepository;
    @Mock
    private CourseResultRepository courseResultRepository;
    @Mock
    private GradeCalculator gradeCalculator;
    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private ResultService resultService;

    private UUID studentId = UUID.randomUUID();
    private UUID sessionId = UUID.randomUUID();
    private Student student;
    private AcademicSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        student = new Student();
        student.setId(studentId);
        User user = new User();
        user.setFullName("Test Student");
        student.setUser(user);

        session = new AcademicSession();
        session.setId(sessionId);
        session.setName("2024/2025");

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        // Mock save to return the same object
        when(resultRepository.save(any(Result.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mock grade calculator
        when(gradeCalculator.calculateGrade(anyDouble())).thenReturn("A");
        when(gradeCalculator.getGradePoint(anyString())).thenReturn(5.0);
        when(gradeCalculator.getRemark(anyString())).thenReturn("Pass");
    }

    @Test
    void processResult_ShouldNotDoubleCount_WhenGhostRecordsExist() {
        // Arrange
        // Current semester registrations (Semester 1, 6 units)
        Course c1 = new Course();
        c1.setUnits(6);
        CourseRegistration reg1 = new CourseRegistration();
        reg1.setId(UUID.randomUUID());
        reg1.setCourse(c1);
        reg1.setSemester(1);
        reg1.setSession(session);

        when(registrationRepository.findByStudentId(studentId)).thenReturn(List.of(reg1));

        Score s1 = new Score();
        s1.setCaScore(30.0);
        s1.setExamScore(50.0);
        when(scoreRepository.findByRegistrationId(reg1.getId())).thenReturn(Optional.of(s1));

        // Mock historical records in DB:
        // 1. A record for the same semester (Semester 1) with "2024/2025" (The ghost)
        Result ghostResult = new Result();
        ghostResult.setSemester(1);
        ghostResult.setTcc(6);
        ghostResult.setTpe(30.0);
        ghostResult.setSession(session);

        // 2. A record with a slightly different but logically same session "2024 2025"
        AcademicSession sessionVariant = new AcademicSession();
        sessionVariant.setName("2024 2025 "); // Variant with spaces
        Result variantResult = new Result();
        variantResult.setSemester(1);
        variantResult.setTcc(6);
        variantResult.setTpe(30.0);
        variantResult.setSession(sessionVariant);

        when(resultRepository.findByStudentId(studentId)).thenReturn(List.of(ghostResult, variantResult));

        // Act
        Result result = resultService.processResult(studentId, sessionId, 1);

        // Assert
        // CCC should be 6, NOT 12 or 18, because they all map to the same
        // super-normalized session
        assertEquals(6, result.getCcc(), "CCC should only count unique semesters");
        assertEquals(30.0, result.getCpe(), "CPE should only count unique semesters");
    }

    @Test
    void deleteAllResults_ShouldInvokeRepositoryDeletion() {
        // Act
        resultService.deleteAllResults();

        // Assert
        verify(courseResultRepository, times(1)).deleteAll();
        verify(resultRepository, times(1)).deleteAll();
    }
}
