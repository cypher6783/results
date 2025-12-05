package com.university.resultsystem.service;

import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.Lecturer;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class LecturerCourseTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetCoursesByLecturerUsername() {
        String username = "lecturer1";
        Course course1 = new Course();
        course1.setCode("CSC101");
        Course course2 = new Course();
        course2.setCode("CSC102");

        when(courseRepository.findByLecturersUserUsername(username)).thenReturn(Arrays.asList(course1, course2));

        List<Course> courses = courseService.getCoursesByLecturerUsername(username);

        assertEquals(2, courses.size());
        assertEquals("CSC101", courses.get(0).getCode());
        assertEquals("CSC102", courses.get(1).getCode());
    }
}
