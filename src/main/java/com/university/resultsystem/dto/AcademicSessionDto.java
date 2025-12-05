package com.university.resultsystem.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AcademicSessionDto {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCurrent;
}
