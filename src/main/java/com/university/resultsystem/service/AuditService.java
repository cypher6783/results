package com.university.resultsystem.service;

import com.university.resultsystem.model.AuditLog;
import com.university.resultsystem.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    @Transactional
    public void logAction(String username, String action, String entityName, String entityId, String changes,
            String ipAddress) {
        AuditLog log = new AuditLog();
        log.setActorUsername(username);
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setChanges(changes);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    @Async
    @Transactional
    public void logAction(String username, String action, String entityName, String entityId, String changes) {
        logAction(username, action, entityName, entityId, changes, null);
    }
}
