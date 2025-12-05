package com.university.resultsystem.controller;

import com.university.resultsystem.model.AcademicSession;
import com.university.resultsystem.service.AcademicSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final AcademicSessionService sessionService;

    public SessionController(AcademicSessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<AcademicSession>> getAllSessions() {
        return ResponseEntity.ok(sessionService.getAllSessions());
    }
}
