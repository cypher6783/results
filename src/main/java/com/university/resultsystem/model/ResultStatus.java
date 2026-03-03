package com.university.resultsystem.model;

public enum ResultStatus {
    DRAFT, // Initial state when scores are being entered
    SUBMITTED, // Lecturer has finished and submitted for vetting
    VETTED, // HOD or Dean has reviewed and confirmed the results
    PUBLISHED // Results are visible to students
}
