package com.university.resultsystem.dto;

import lombok.Data;

@Data
public class CourseDto {
    private String code;
    private String title;
    private Integer units;
    private Integer semester;
    private Integer level;
    private String department;
    private java.util.UUID lecturerId;
    private String lecturerStaffId;
}
