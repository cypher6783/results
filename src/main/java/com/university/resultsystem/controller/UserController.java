package com.university.resultsystem.controller;

import com.university.resultsystem.dto.PasswordChangeDto;
import com.university.resultsystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDto dto, Authentication authentication) {
        try {
            userService.changePassword(authentication.getName(), dto);
            return ResponseEntity.ok().body("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
