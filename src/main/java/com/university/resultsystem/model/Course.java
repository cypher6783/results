package com.university.resultsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Data
public class Course {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer units;

    @Column(nullable = false)
    private Integer semester; // 1 or 2

    @Column(nullable = false)
    private Integer level; // e.g., "100"

    @Column(nullable = false)
    private String department;

    @ManyToMany
    @JoinTable(name = "course_lecturers", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "lecturer_id"))
    private List<Lecturer> lecturers = new ArrayList<>();
}
