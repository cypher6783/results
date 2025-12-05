package com.university.resultsystem.dto;

import lombok.Data;

@Data
public class StudentRegistrationDto {
    private String fullName;
    private String matricNo;
    private String level; // e.g., "100", "200"
    private String department;
}
