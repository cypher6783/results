package com.university.resultsystem.dto;

import lombok.Data;

@Data
public class AdminPasswordChangeDto {
    private String username;
    private String newPassword;
}
