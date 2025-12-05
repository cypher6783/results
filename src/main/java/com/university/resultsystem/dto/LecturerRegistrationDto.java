package com.university.resultsystem.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LecturerRegistrationDto {
    private String fullName;
    private String staffId;
    private List<UUID> courseIds; // Optional: courses to assign
}
