package com.university.resultsystem.controller;

import com.university.resultsystem.dto.LecturerRegistrationDto;
import com.university.resultsystem.dto.StudentRegistrationDto;
import com.university.resultsystem.model.AcademicSession;
import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.Lecturer;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.repository.AcademicSessionRepository;
import com.university.resultsystem.repository.CourseRepository;
import com.university.resultsystem.repository.LecturerRepository;
import com.university.resultsystem.repository.StudentRepository;
import com.university.resultsystem.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final BulkUploadService bulkUploadService;
    private final CourseService courseService;
    private final ResultService resultService;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AcademicSessionRepository sessionRepository;
    private final PdfService pdfService;

    public AdminController(UserService userService,
            BulkUploadService bulkUploadService,
            CourseService courseService,
            ResultService resultService,
            StudentRepository studentRepository,
            LecturerRepository lecturerRepository,
            AcademicSessionRepository sessionRepository,
            PdfService pdfService) {
        this.userService = userService;
        this.bulkUploadService = bulkUploadService;
        this.courseService = courseService;
        this.resultService = resultService;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.sessionRepository = sessionRepository;
        this.pdfService = pdfService;
    }

    // Student Management
    @PostMapping("/students")
    public ResponseEntity<?> addStudent(@RequestBody StudentRegistrationDto studentDto) {
        try {
            return ResponseEntity.ok(userService.createStudent(studentDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/students/bulk")
    public ResponseEntity<String> bulkUploadStudents(@RequestParam("file") MultipartFile file) {
        try {
            bulkUploadService.uploadStudents(file);
            return ResponseEntity.ok("Successfully uploaded students from CSV");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload students: " + e.getMessage());
        }
    }

    @GetMapping("/students")
    public ResponseEntity<List<com.university.resultsystem.model.User>> getAllStudents() {
        return ResponseEntity.ok(userService.getAllStudents());
    }

    // Lecturer Management
    @PostMapping("/lecturers")
    public ResponseEntity<?> addLecturer(@RequestBody LecturerRegistrationDto lecturerDto) {
        try {
            return ResponseEntity.ok(userService.createLecturer(lecturerDto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/lecturers")
    public ResponseEntity<List<com.university.resultsystem.model.User>> getAllLecturers() {
        return ResponseEntity.ok(userService.getAllLecturers());
    }

    @GetMapping("/lecturers/entities")
    public ResponseEntity<List<Lecturer>> getAllLecturerEntities() {
        return ResponseEntity.ok(lecturerRepository.findAll());
    }

    // Course Management
    @PostMapping("/courses")
    public ResponseEntity<?> addCourse(@RequestBody Map<String, Object> payload) {
        try {
            Course course = new Course();
            course.setCode((String) payload.get("code"));
            course.setTitle((String) payload.get("title"));
            course.setUnits((Integer) payload.get("units"));
            course.setSemester((Integer) payload.get("semester"));
            course.setLevel((Integer) payload.get("level"));
            course.setDepartment((String) payload.get("department"));

            String lecturerIdStr = (String) payload.get("lecturerId");
            UUID lecturerId = (lecturerIdStr != null && !lecturerIdStr.isEmpty()) ? UUID.fromString(lecturerIdStr)
                    : null;

            return ResponseEntity.ok(courseService.createCourse(course, lecturerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/courses/bulk")
    public ResponseEntity<String> bulkUploadCourses(@RequestParam("file") MultipartFile file) {
        try {
            bulkUploadService.uploadCourses(file);
            return ResponseEntity.ok("Successfully uploaded courses from CSV");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to upload courses: " + e.getMessage());
        }
    }

    @GetMapping("/courses")
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<?> updateCourse(@PathVariable UUID id, @RequestBody Map<String, Object> payload) {
        try {
            Course course = new Course();
            course.setCode((String) payload.get("code"));
            course.setTitle((String) payload.get("title"));
            course.setUnits((Integer) payload.get("units"));
            course.setSemester((Integer) payload.get("semester"));
            course.setLevel((Integer) payload.get("level"));
            course.setDepartment((String) payload.get("department"));

            String lecturerIdStr = (String) payload.get("lecturerId");
            UUID lecturerId = (lecturerIdStr != null && !lecturerIdStr.isEmpty()) ? UUID.fromString(lecturerIdStr)
                    : null;

            return ResponseEntity.ok(courseService.updateCourse(id, course, lecturerId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<?> deleteCourse(@PathVariable UUID id) {
        try {
            courseService.deleteCourse(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Session Management
    @GetMapping("/sessions")
    public ResponseEntity<List<AcademicSession>> getAllSessions() {
        return ResponseEntity.ok(sessionRepository.findAll());
    }

    @PostMapping("/sessions")
    public ResponseEntity<AcademicSession> createSession(@RequestBody AcademicSession session) {
        return ResponseEntity.ok(sessionRepository.save(session));
    }

    // Admin Password Reset
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody com.university.resultsystem.dto.AdminPasswordChangeDto dto) {
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

    @PostMapping("/publish-results")
    public ResponseEntity<?> publishResults(@RequestParam UUID sessionId, @RequestParam Integer semester) {
        try {
            resultService.publishResults(sessionId, semester);
            return ResponseEntity.ok("Results published successfully");
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

    @PostMapping("/reset-results")
    public ResponseEntity<?> resetAllResults() {
        try {
            resultService.deleteAllResults();
            return ResponseEntity.ok("All processed results have been deleted successfully. You can now start afresh.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
