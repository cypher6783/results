package com.university.resultsystem.service;

import com.university.resultsystem.dto.LecturerRegistrationDto;
import com.university.resultsystem.dto.PasswordChangeDto;
import com.university.resultsystem.dto.StudentRegistrationDto;
import com.university.resultsystem.dto.UserRegistrationDto;
import com.university.resultsystem.model.Lecturer;
import com.university.resultsystem.model.Role;
import com.university.resultsystem.model.Student;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.LecturerRepository;
import com.university.resultsystem.repository.StudentRepository;
import com.university.resultsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, StudentRepository studentRepository,
            LecturerRepository lecturerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());

        User savedUser = userRepository.save(user);

        if (dto.getRole() == Role.STUDENT) {
            Student student = new Student();
            student.setUser(savedUser);
            student.setMatricNo(dto.getMatricNo());
            student.setDepartment(dto.getDepartment());
            student.setLevel(dto.getLevel());
            student.setEntryYear(dto.getEntryYear());
            studentRepository.save(student);
        }

        return savedUser;
    }

    @Transactional
    public User createStudent(StudentRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getMatricNo())) {
            throw new RuntimeException("Student with this matric number already exists");
        }

        // Create User with matric number as username and password
        User user = new User();
        user.setUsername(dto.getMatricNo());
        user.setPasswordHash(passwordEncoder.encode(dto.getMatricNo())); // Default password = matric number
        user.setFullName(dto.getFullName());
        user.setRole(Role.STUDENT);
        user.setPasswordChanged(false); // User must change password on first login

        User savedUser = userRepository.save(user);

        // Create Student record
        Student student = new Student();
        student.setUser(savedUser);
        student.setMatricNo(dto.getMatricNo());
        student.setDepartment(dto.getDepartment());
        student.setLevel(dto.getLevel());
        student.setEntryYear(LocalDateTime.now().getYear()); // Set current year as entry year
        studentRepository.save(student);

        return savedUser;
    }

    @Transactional
    public User createLecturer(LecturerRegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getStaffId())) {
            throw new RuntimeException("Lecturer with this staff ID already exists");
        }

        // Create User with staff ID as username and password
        User user = new User();
        user.setUsername(dto.getStaffId());
        user.setPasswordHash(passwordEncoder.encode(dto.getStaffId())); // Default password = staff ID
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setRole(Role.LECTURER);
        user.setPasswordChanged(false); // User must change password on first login

        User savedUser = userRepository.save(user);

        // Create Lecturer record
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(savedUser);
        lecturer.setStaffId(dto.getStaffId());
        lecturerRepository.save(lecturer);

        return savedUser;
    }

    @Transactional
    public void changePassword(String username, PasswordChangeDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate confirmation password
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new RuntimeException("New password and confirmation password do not match");
        }

        // Verify old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setPasswordChanged(true);
        userRepository.save(user);
    }

    public List<User> getAllStudents() {
        return userRepository.findByRole(Role.STUDENT);
    }

    public List<User> getAllLecturers() {
        return userRepository.findByRole(Role.LECTURER);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void adminChangePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Admin can change password without old password verification
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(false); // Force user to change password on next login
        userRepository.save(user);
    }
}
