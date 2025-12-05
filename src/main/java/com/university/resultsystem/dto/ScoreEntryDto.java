package com.university.resultsystem.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ScoreEntryDto {
    private UUID studentId;
    private String matricNo; // Alternative to studentId
    private UUID courseId;
    private UUID sessionId;
    private Double caScore;
    private Double examScore;
}
