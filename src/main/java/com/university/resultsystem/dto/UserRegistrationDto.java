package com.university.resultsystem.dto;

import com.university.resultsystem.model.Role;
import lombok.Data;

@Data
public class UserRegistrationDto {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private Role role;
    
    // Student specific
    private String matricNo;
    private String department;
    private String level;
    private Integer entryYear;
}
