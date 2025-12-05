package com.university.resultsystem.dto;

import lombok.Data;

@Data
public class CourseResultDto {
    private String code;
    private String title;
    private Integer unit;
    private Double score;
    private Double pointEarned;
    private String grade;
    private String remark;
}
