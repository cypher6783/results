package com.university.resultsystem.controller;

import com.university.resultsystem.dto.ScoreEntryDto;
import com.university.resultsystem.service.BulkService;
import com.university.resultsystem.service.ScoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lecturer")
public class LecturerController {

    private final ScoreService scoreService;
    private final BulkService bulkService;
    private final com.university.resultsystem.repository.StudentRepository studentRepository;

    public LecturerController(ScoreService scoreService, BulkService bulkService,
            com.university.resultsystem.repository.StudentRepository studentRepository,
            com.university.resultsystem.service.CourseService courseService) {
        this.scoreService = scoreService;
        this.bulkService = bulkService;
        this.studentRepository = studentRepository;
        this.courseService = courseService;
    }

    private final com.university.resultsystem.service.CourseService courseService;

    @org.springframework.web.bind.annotation.GetMapping("/courses")
    public ResponseEntity<List<com.university.resultsystem.model.Course>> getLecturerCourses() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(courseService.getCoursesByLecturerUsername(auth.getName()));
    }

    @PostMapping("/scores")
    public ResponseEntity<?> enterScore(@RequestBody ScoreEntryDto dto) {
        try {
            // If matricNo is provided, look up the student ID
            if (dto.getMatricNo() != null && !dto.getMatricNo().isEmpty()) {
                com.university.resultsystem.model.Student student = studentRepository.findByMatricNo(dto.getMatricNo())
                        .orElseThrow(() -> new RuntimeException(
                                "Student not found with matric number: " + dto.getMatricNo()));
                dto.setStudentId(student.getId());
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            scoreService.saveScore(dto, auth.getName());
            return ResponseEntity.ok().body("Score submitted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/scores/bulk")
    public ResponseEntity<?> uploadBulkScores(@RequestParam("file") MultipartFile file,
            @RequestParam("courseId") UUID courseId,
            @RequestParam("sessionId") UUID sessionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<ScoreEntryDto> scores = bulkService.parseScoreCsv(file, courseId, sessionId);

        for (ScoreEntryDto dto : scores) {
            scoreService.saveScore(dto, auth.getName());
        }

        return ResponseEntity.ok().body("Processed " + scores.size() + " scores.");
    }
}
