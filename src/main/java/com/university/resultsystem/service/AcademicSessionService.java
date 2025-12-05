package com.university.resultsystem.service;

import com.university.resultsystem.dto.AcademicSessionDto;
import com.university.resultsystem.model.AcademicSession;
import com.university.resultsystem.repository.AcademicSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AcademicSessionService {

    private final AcademicSessionRepository sessionRepository;

    public AcademicSessionService(AcademicSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public AcademicSession createSession(AcademicSessionDto dto) {
        if (sessionRepository.findByName(dto.getName()).isPresent()) {
            throw new RuntimeException("Session already exists");
        }

        if (dto.isCurrent()) {
            // Unset current flag for other sessions
            sessionRepository.findByIsCurrentTrue().ifPresent(s -> {
                s.setCurrent(false);
                sessionRepository.save(s);
            });
        }

        AcademicSession session = new AcademicSession();
        session.setName(dto.getName());
        session.setStartDate(dto.getStartDate());
        session.setEndDate(dto.getEndDate());
        session.setCurrent(dto.isCurrent());
        return sessionRepository.save(session);
    }

    public AcademicSession getCurrentSession() {
        return sessionRepository.findByIsCurrentTrue()
                .orElseThrow(() -> new RuntimeException("No active session found"));
    }

    public List<AcademicSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    @Transactional
    public AcademicSession labelSession(java.util.UUID sessionId) {
        AcademicSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setLabeled(true);
        return sessionRepository.save(session);
    }

    @Transactional
    public AcademicSession unlabelSession(java.util.UUID sessionId) {
        AcademicSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setLabeled(false);
        return sessionRepository.save(session);
    }
}
