package com.university.resultsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "results", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "student_id", "session_id", "semester" })
})
@Data
public class Result {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AcademicSession session;

    @Column(nullable = false)
    private Integer semester;

    // Current Semester Metrics
    @Column
    private Integer tcc; // Total Credit Carried

    @Column
    private Integer tce; // Total Credit Earned

    @Column
    private Double tpe; // Total Point Earned

    @Column
    private Double gpa;

    // Previous Semester Metrics
    @Column
    private Integer previousTcc;

    @Column
    private Integer previousTce;

    @Column
    private Double previousTpe;

    @Column
    private Double previousGpa;

    // Cumulative Metrics
    @Column
    private Integer ccc; // Cumulative Credit Carried

    @Column
    private Integer cce; // Cumulative Credit Earned

    @Column
    private Double cpe; // Cumulative Point Earned

    @Column
    private Double cgpa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("'DRAFT'")
    private ResultStatus approvalStatus = ResultStatus.DRAFT;

    @Column(nullable = false)
    private String status; // PASS, FAIL, PROBATION

    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    private boolean published = false;

    // Legacy fields (kept for backward compatibility)
    @Column
    private Integer totalUnits;

    @Column
    private Double totalPoints;
}
