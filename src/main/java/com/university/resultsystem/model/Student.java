package com.university.resultsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "students")
@Data
public class Student {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String matricNo;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String level;

    @Column(nullable = false)
    private Integer entryYear;
}
