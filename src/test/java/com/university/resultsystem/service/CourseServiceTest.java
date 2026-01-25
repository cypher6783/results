package com.university.resultsystem.service;

import com.university.resultsystem.dto.CourseDto;
import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.Lecturer;
import com.university.resultsystem.repository.CourseRepository;
import com.university.resultsystem.repository.LecturerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LecturerRepository lecturerRepository;

    @Mock
    private com.university.resultsystem.repository.CourseRegistrationRepository registrationRepository;

    @Mock
    private com.university.resultsystem.repository.ScoreRepository scoreRepository;

    @Mock
    private com.university.resultsystem.repository.CourseResultRepository courseResultRepository;

    @InjectMocks
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCourse_ShouldAssignLecturer_WhenLecturerIdIsProvided() {
        // Arrange
        UUID lecturerId = UUID.randomUUID();
        CourseDto dto = new CourseDto();
        dto.setCode("CSC101");
        dto.setTitle("Intro to CS");
        dto.setUnits(3);
        dto.setSemester(1);
        dto.setLevel(100);
        dto.setDepartment("Computer Science");
        dto.setLecturerId(lecturerId);

        Lecturer lecturer = new Lecturer();
        lecturer.setId(lecturerId);

        when(lecturerRepository.findById(lecturerId)).thenReturn(Optional.of(lecturer));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course createdCourse = courseService.createCourse(dto);

        // Assert
        assertNotNull(createdCourse);
        assertEquals("CSC101", createdCourse.getCode());
        assertEquals(1, createdCourse.getLecturers().size());
        assertEquals(lecturerId, createdCourse.getLecturers().get(0).getId());
        verify(lecturerRepository, times(1)).findById(lecturerId);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void createCourse_ShouldNotAssignLecturer_WhenLecturerIdIsNull() {
        // Arrange
        CourseDto dto = new CourseDto();
        dto.setCode("MTH101");
        dto.setTitle("Mathematics");
        dto.setUnits(3);
        dto.setSemester(1);
        dto.setLevel(100);
        dto.setDepartment("Mathematics");
        dto.setLecturerId(null);

        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course createdCourse = courseService.createCourse(dto);

        // Assert
        assertNotNull(createdCourse);
        assertEquals("MTH101", createdCourse.getCode());
        assertTrue(createdCourse.getLecturers().isEmpty());
        verify(lecturerRepository, never()).findById(any());
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void createCourse_ShouldAssignLecturer_WhenLecturerStaffIdIsProvided() {
        // Arrange
        String staffId = "STAFF123";
        CourseDto dto = new CourseDto();
        dto.setCode("CSC201");
        dto.setTitle("Data Structures");
        dto.setUnits(3);
        dto.setSemester(1);
        dto.setLevel(200);
        dto.setDepartment("Computer Science");
        dto.setLecturerStaffId(staffId);

        Lecturer lecturer = new Lecturer();
        lecturer.setStaffId(staffId);

        when(lecturerRepository.findByStaffId(staffId)).thenReturn(Optional.of(lecturer));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course createdCourse = courseService.createCourse(dto);

        // Assert
        assertNotNull(createdCourse);
        assertEquals("CSC201", createdCourse.getCode());
        assertEquals(1, createdCourse.getLecturers().size());
        assertEquals(staffId, createdCourse.getLecturers().get(0).getStaffId());
        verify(lecturerRepository, times(1)).findByStaffId(staffId);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void updateCourse_ShouldUpdateBasicDetails() {
        // Arrange
        UUID courseId = UUID.randomUUID();
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setCode("OLD101");

        CourseDto dto = new CourseDto();
        dto.setCode("NEW101");
        dto.setTitle("New Title");
        dto.setUnits(4);
        dto.setSemester(2);
        dto.setLevel(300);
        dto.setDepartment("New Dept");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Course updatedCourse = courseService.updateCourse(courseId, dto);

        // Assert
        assertEquals("NEW101", updatedCourse.getCode());
        assertEquals("New Title", updatedCourse.getTitle());
        assertEquals(4, updatedCourse.getUnits());
        assertEquals(2, updatedCourse.getSemester());
        assertEquals(300, updatedCourse.getLevel());
        assertEquals("New Dept", updatedCourse.getDepartment());
    }

    @Test
    void deleteCourse_ShouldInvokeCleanupAndRepositoryDeletion() {
        // Arrange
        UUID courseId = UUID.randomUUID();
        when(courseRepository.existsById(courseId)).thenReturn(true);

        // Act
        courseService.deleteCourse(courseId);

        // Assert
        verify(scoreRepository, times(1)).deleteByRegistrationCourseId(courseId);
        verify(registrationRepository, times(1)).deleteByCourseId(courseId);
        verify(courseResultRepository, times(1)).deleteByCourseId(courseId);
        verify(courseRepository, times(1)).deleteById(courseId);
    }
}
