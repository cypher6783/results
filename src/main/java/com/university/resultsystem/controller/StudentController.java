package com.university.resultsystem.controller;

import com.university.resultsystem.dto.DetailedResultDto;
import com.university.resultsystem.dto.ResultDto;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.StudentRepository;
import com.university.resultsystem.repository.UserRepository;
import com.university.resultsystem.service.PdfService;
import com.university.resultsystem.service.ResultService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final ResultService resultService;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PdfService pdfService;

    public StudentController(ResultService resultService, StudentRepository studentRepository,
            UserRepository userRepository, PdfService pdfService) {
        this.resultService = resultService;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.pdfService = pdfService;
    }

    @GetMapping("/result")
    public ResponseEntity<ResultDto> getResult(@RequestParam UUID sessionId, @RequestParam Integer semester) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        return ResponseEntity.ok(resultService.getResultDto(student.getId(), sessionId, semester));
    }

    @GetMapping("/result/detailed")
    public ResponseEntity<DetailedResultDto> getDetailedResult(@RequestParam UUID sessionId,
            @RequestParam Integer semester) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        return ResponseEntity.ok(resultService.getDetailedResult(student.getId(), sessionId, semester));
    }

    @GetMapping("/result/pdf")
    public ResponseEntity<byte[]> downloadResultPdf(@RequestParam UUID sessionId, @RequestParam Integer semester) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found"));

        DetailedResultDto result = resultService.getDetailedResult(student.getId(), sessionId, semester);

        try {
            byte[] pdfBytes = pdfService.generateResultSlip(result);

            String filename = "result_" + result.getMatricNo() + "_" + result.getSessionName().replace("/", "-")
                    + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
