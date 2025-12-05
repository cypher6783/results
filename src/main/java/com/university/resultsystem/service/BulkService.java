package com.university.resultsystem.service;

import com.university.resultsystem.dto.ScoreEntryDto;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BulkService {

    private final StudentRepository studentRepository;

    public BulkService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<ScoreEntryDto> parseScoreCsv(MultipartFile file, UUID courseId, UUID sessionId) {
        List<ScoreEntryDto> scores = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                String matricNo = parts[0].trim();
                double ca = Double.parseDouble(parts[1].trim());
                double exam = Double.parseDouble(parts[2].trim());

                Student student = studentRepository.findByMatricNo(matricNo)
                        .orElseThrow(() -> new RuntimeException("Student not found: " + matricNo));

                ScoreEntryDto dto = new ScoreEntryDto();
                dto.setStudentId(student.getId());
                dto.setCourseId(courseId);
                dto.setSessionId(sessionId);
                dto.setCaScore(ca);
                dto.setExamScore(exam);
                scores.add(dto);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV: " + e.getMessage());
        }
        return scores;
    }
}
