package com.university.resultsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "scores")
@Data
public class Score {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "registration_id", unique = true, nullable = false)
    private CourseRegistration registration;

    @Column(nullable = false)
    private Double caScore;

    @Column(nullable = false)
    private Double examScore;

    @Column(nullable = false)
    private Double totalScore;

    @Column(nullable = false)
    private String grade; // A, B, C, D, F

    @Column(nullable = false)
    private Double gradePoint; // 5.0, 4.0, etc.

    @Column(nullable = false)
    private boolean isPublished = false;

    @Version
    private Integer version; // Optimistic locking
}
