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
}
