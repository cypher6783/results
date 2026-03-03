package com.university.resultsystem.service;

import com.university.resultsystem.dto.StudentRegistrationDto;
import com.university.resultsystem.model.Course;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class BulkUploadService {

    private final UserService userService;
    private final CourseService courseService;
    private final StudentRepository studentRepository;

    public BulkUploadService(UserService userService, CourseService courseService,
            StudentRepository studentRepository) {
        this.userService = userService;
        this.courseService = courseService;
        this.studentRepository = studentRepository;
    }

    @Async
    @Transactional
    public void uploadStudentsAsync(List<String> lines) {
        log.info("Starting asynchronous student upload for {} lines", lines.size());
        try {
            boolean isFirstLine = true;
            for (String line : lines) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] fields = line.split(",");
                if (fields.length < 4)
                    continue;

                StudentRegistrationDto dto = new StudentRegistrationDto();
                dto.setFullName(fields[0].trim());
                dto.setMatricNo(fields[1].trim());
                dto.setLevel(fields[2].trim());
                dto.setDepartment(fields[3].trim());

                userService.createStudent(dto);
            }
            log.info("Asynchronous student upload completed successfully");
        } catch (Exception e) {
            log.error("Error in asynchronous student upload: {}", e.getMessage(), e);
        }
    }

    @Async
    @Transactional
    public void uploadCoursesAsync(List<String> lines) {
        log.info("Starting asynchronous course upload for {} lines", lines.size());
        try {
            boolean isFirstLine = true;
            for (String line : lines) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty())
                    continue;

                String[] fields = line.split(",");
                if (fields.length < 6)
                    continue;

                com.university.resultsystem.dto.CourseDto dto = new com.university.resultsystem.dto.CourseDto();
                dto.setCode(fields[0].trim());
                dto.setTitle(fields[1].trim());
                dto.setUnits(Integer.parseInt(fields[2].trim()));
                dto.setSemester(Integer.parseInt(fields[3].trim()));
                dto.setLevel(Integer.parseInt(fields[4].trim()));
                dto.setDepartment(fields[5].trim());

                if (fields.length >= 7 && !fields[6].trim().isEmpty()) {
                    dto.setLecturerStaffId(fields[6].trim());
                }

                courseService.createCourse(dto);
            }
            log.info("Asynchronous course upload completed successfully");
        } catch (Exception e) {
            log.error("Error in asynchronous course upload: {}", e.getMessage(), e);
        }
    }
}
