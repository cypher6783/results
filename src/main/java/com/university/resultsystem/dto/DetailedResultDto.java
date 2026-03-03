package com.university.resultsystem.dto;

import lombok.Data;
import java.util.List;
import com.university.resultsystem.dto.CourseResultDto;

@Data
public class DetailedResultDto {
    // Student info
    private String matricNo;
    private String fullName;
    private String course;
    private String level;

    // Session info
    private String sessionName;
    private Integer semester;

    // Course list
    private List<CourseResultDto> courses;

    // Current semester metrics
    private Integer tcc; // Total Credit Carried
    private Integer tce; // Total Credit Earned
    private Double tpe; // Total Point Earned
    private Double gpa;

    // Previous semester metrics
    private Integer previousTcc;
    private Integer previousTce;
    private Double previousTpe;
    private Double previousGpa;

    // Cumulative metrics
    private Integer ccc; // Cumulative Credit Carried
    private Integer cce; // Cumulative Credit Earned
    private Double cpe; // Cumulative Point Earned
    private Double cgpa;

    private String status;
    private String approvalStatus;
}
