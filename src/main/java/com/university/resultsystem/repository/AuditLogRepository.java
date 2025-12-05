package com.university.resultsystem.repository;

import com.university.resultsystem.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByActorUsername(String actorUsername);
    List<AuditLog> findByEntityNameAndEntityId(String entityName, String entityId);
}
