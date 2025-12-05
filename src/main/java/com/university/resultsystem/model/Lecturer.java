package com.university.resultsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "lecturers")
@Data
public class Lecturer {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(unique = true, nullable = false)
    private String staffId;

    @ManyToMany(mappedBy = "lecturers")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Course> courses = new ArrayList<>();
}
