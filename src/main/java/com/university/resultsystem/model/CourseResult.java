package com.university.resultsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "course_results")
@Data
public class CourseResult {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private Result result;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private Integer unit;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private Double pointEarned;

    @Column(nullable = false)
    private String grade;

    @Column(nullable = false)
    private String remark; // "Pass" or "Fail"
}
