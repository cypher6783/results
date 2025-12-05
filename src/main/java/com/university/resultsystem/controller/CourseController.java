package com.university.resultsystem.controller;

import com.university.resultsystem.model.Course;
import com.university.resultsystem.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @org.springframework.web.bind.annotation.PostMapping
    public ResponseEntity<Course> createCourse(
            @org.springframework.web.bind.annotation.RequestBody com.university.resultsystem.dto.CourseDto courseDto) {
        return ResponseEntity.ok(courseService.createCourse(courseDto));
    }
}
