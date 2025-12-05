package com.university.resultsystem.controller;

import com.university.resultsystem.dto.AcademicSessionDto;
import com.university.resultsystem.dto.CourseDto;
import com.university.resultsystem.model.AcademicSession;
import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.User;
import com.university.resultsystem.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;
    private final AcademicSessionService sessionService;
    private final RegistrationService registrationService;
    private final ResultService resultService;
    private final com.university.resultsystem.repository.LecturerRepository lecturerRepository;
    private final PdfService pdfService;
    private final com.university.resultsystem.repository.StudentRepository studentRepository;

    private final com.university.resultsystem.service.BulkUploadService bulkService;

    public AdminController(UserService userService, CourseService courseService, AcademicSessionService sessionService,
            RegistrationService registrationService, ResultService resultService,
            com.university.resultsystem.repository.LecturerRepository lecturerRepository, PdfService pdfService,
            com.university.resultsystem.repository.StudentRepository studentRepository,
            com.university.resultsystem.service.BulkUploadService bulkService) {
        this.userService = userService;
        this.courseService = courseService;
        this.sessionService = sessionService;
        this.registrationService = registrationService;
        this.resultService = resultService;
        this.lecturerRepository = lecturerRepository;
        this.pdfService = pdfService;
        this.studentRepository = studentRepository;
        this.bulkService = bulkService;
    }

    // User Management
    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@RequestBody com.university.resultsystem.dto.StudentRegistrationDto dto) {
        try {
            return ResponseEntity.ok(userService.createStudent(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/lecturers")
    public ResponseEntity<?> createLecturer(@RequestBody com.university.resultsystem.dto.LecturerRegistrationDto dto) {
        try {
            return ResponseEntity.ok(userService.createLecturer(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/students")
    public ResponseEntity<List<User>> getAllStudents() {
        return ResponseEntity.ok(userService.getAllStudents());
    }

    @GetMapping("/lecturers")
    public ResponseEntity<List<User>> getAllLecturers() {
        return ResponseEntity.ok(userService.getAllLecturers());
    }

    @GetMapping("/lecturers/entities")
    public ResponseEntity<List<com.university.resultsystem.model.Lecturer>> getAllLecturerEntities() {
        return ResponseEntity.ok(lecturerRepository.findAll());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Lecturer Assignment
    @PostMapping("/courses/{courseId}/lecturers")
    public ResponseEntity<?> assignLecturers(@PathVariable UUID courseId, @RequestBody List<UUID> lecturerIds) {
        try {
            return ResponseEntity.ok(courseService.assignLecturers(courseId, lecturerIds));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bulk Uploads
    @PostMapping("/students/bulk")
    public ResponseEntity<?> bulkUploadStudents(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            List<User> students = bulkService.parseAndCreateStudents(file);
            return ResponseEntity.ok("Successfully created " + students.size() + " students");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/courses/bulk")
    public ResponseEntity<?> bulkUploadCourses(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            List<Course> courses = bulkService.parseAndCreateCourses(file);
            return ResponseEntity.ok("Successfully created " + courses.size() + " courses");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Session Labeling
    @PostMapping("/sessions/{sessionId}/label")
    public ResponseEntity<?> labelSession(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(sessionService.labelSession(sessionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sessions/{sessionId}/unlabel")
    public ResponseEntity<?> unlabelSession(@PathVariable UUID sessionId) {
        try {
            return ResponseEntity.ok(sessionService.unlabelSession(sessionId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Course Management
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CourseDto dto) {
        try {
            return ResponseEntity.ok(courseService.createCourse(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // Session Management
    @PostMapping("/sessions")
    public ResponseEntity<AcademicSession> createSession(@RequestBody AcademicSessionDto dto) {
        return ResponseEntity.ok(sessionService.createSession(dto));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<AcademicSession>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }

    // Registration
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestParam UUID studentId, @RequestParam UUID courseId,
            @RequestParam UUID sessionId) {
        return ResponseEntity.ok(registrationService.registerStudent(studentId, courseId, sessionId));
    }

    // Result Processing
    @PostMapping("/process-results")
    public ResponseEntity<?> processResults(@RequestParam UUID studentId, @RequestParam UUID sessionId,
            @RequestParam Integer semester) {
        return ResponseEntity.ok(resultService.processResult(studentId, sessionId, semester));
    }

    // Admin Password Change
    @PostMapping("/change-password")
    public ResponseEntity<?> adminChangePassword(
            @RequestBody com.university.resultsystem.dto.AdminPasswordChangeDto dto) {
        try {
            userService.adminChangePassword(dto.getUsername(), dto.getNewPassword());
            return ResponseEntity.ok().body("Password changed successfully. User must change password on next login.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bulk Result Generation
    @PostMapping("/bulk-results")
    public ResponseEntity<?> processBulkResults(@RequestParam UUID sessionId, @RequestParam Integer semester) {
        try {
            return ResponseEntity.ok(resultService.processBulkResults(sessionId, semester));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Batch PDF Generation
    @GetMapping("/bulk-results/pdf")
    public ResponseEntity<byte[]> generateBatchResultsPdf(@RequestParam UUID sessionId,
            @RequestParam Integer semester) {
        try {
            // Get all students
            List<com.university.resultsystem.model.Student> students = studentRepository.findAll();

            // Get detailed results for all students
            java.util.List<com.university.resultsystem.dto.DetailedResultDto> results = new java.util.ArrayList<>();
            for (com.university.resultsystem.model.Student student : students) {
                try {
                    com.university.resultsystem.dto.DetailedResultDto result = resultService
                            .getDetailedResult(student.getId(), sessionId, semester);
                    results.add(result);
                } catch (Exception e) {
                    // Skip students without results
                    continue;
                }
            }

            byte[] pdfBytes = pdfService.generateBatchResultsPdf(results);

            String filename = "batch_results_" + sessionId + "_semester" + semester + ".pdf";

            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + filename)
                    .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
