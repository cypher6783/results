package com.university.resultsystem.service;

import com.university.resultsystem.model.AuditLog;
import com.university.resultsystem.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(String username, String action, String entityName, String entityId, String changes) {
        AuditLog log = new AuditLog();
        log.setActorUsername(username);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setChanges(changes);
        auditLogRepository.save(log);
    }
}
