package com.university.resultsystem.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ResultDto {
    private UUID studentId;
    private String matricNo;
    private String studentName;
    private UUID sessionId;
    private String sessionName;
    private Integer semester;
    private Double gpa;
    private Double cgpa;
    private String status;
    private String remarks; // PASS or FAIL based on GPA/Status
}
