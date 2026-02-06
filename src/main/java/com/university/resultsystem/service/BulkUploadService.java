package com.university.resultsystem.service;

import com.university.resultsystem.dto.StudentRegistrationDto;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

    @Transactional
    public List<User> uploadStudents(MultipartFile file) {
        List<User> createdStudents = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length < 4) {
                    throw new RuntimeException("Invalid CSV format. Expected: FullName,MatricNo,Level,Department");
                }

                StudentRegistrationDto dto = new StudentRegistrationDto();
                dto.setFullName(fields[0].trim());
                dto.setMatricNo(fields[1].trim());
                dto.setLevel(fields[2].trim());
                dto.setDepartment(fields[3].trim());

                User student = userService.createStudent(dto);
                createdStudents.add(student);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV file: " + e.getMessage());
        }

        return createdStudents;
    }

    @Transactional
    public List<com.university.resultsystem.model.Course> uploadCourses(MultipartFile file) {
        List<com.university.resultsystem.model.Course> createdCourses = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length < 6) {
                    throw new RuntimeException(
                            "Invalid CSV format. Expected: Code,Title,Units,Semester,Level,Department[,LecturerStaffId]");
                }

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

                com.university.resultsystem.model.Course course = courseService.createCourse(dto);
                createdCourses.add(course);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV file: " + e.getMessage());
        }

        return createdCourses;
    }
}
