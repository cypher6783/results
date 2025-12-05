package com.university.resultsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String actorUsername;

    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, LOGIN

    @Column(nullable = false)
    private String entityName;

    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String changes; // JSON string of changes

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
