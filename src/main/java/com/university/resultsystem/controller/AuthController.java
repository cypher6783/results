package com.university.resultsystem.controller;

import com.university.resultsystem.model.PasswordResetToken;
import com.university.resultsystem.model.User;
import com.university.resultsystem.repository.PasswordResetTokenRepository;
import com.university.resultsystem.repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, AuthenticationManager authenticationManager,
            PasswordResetTokenRepository tokenRepository, JavaMailSender mailSender,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password,
            HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isStaff = user.getRole().name().equals("ADMIN") || user.getRole().name().equals("LECTURER");

            if (isStaff) {
                HttpSession session = request.getSession();
                session.setAttribute("PRE_AUTH_USER", authentication);

                Map<String, Object> response = new HashMap<>();
                if (!user.isMfaEnabled()) {
                    String secret = secretGenerator.generate();
                    user.setMfaSecret(secret);
                    userRepository.save(user);

                    QrData data = new QrData.Builder()
                            .label(user.getEmail() != null ? user.getEmail() : user.getUsername())
                            .secret(secret)
                            .issuer("University Result System")
                            .algorithm(HashingAlgorithm.SHA1)
                            .digits(6)
                            .period(30)
                            .build();

                    QrGenerator generator = new ZxingPngQrGenerator();
                    byte[] imageData = generator.generate(data);
                    String mimeType = generator.getImageMimeType();
                    String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageData);

                    response.put("mfaSetupRequired", true);
                    response.put("qrCode", dataUri);
                    return ResponseEntity.ok(response);
                } else {
                    response.put("mfaRequired", true);
                    return ResponseEntity.ok(response);
                }
            }

            // For Students, just log them in directly
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestParam String code, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("PRE_AUTH_USER") == null) {
            return ResponseEntity.status(401).body("Session expired or invalid flow.");
        }

        Authentication authentication = (Authentication) session.getAttribute("PRE_AUTH_USER");
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body("User not found.");
        }

        if (codeVerifier.isValidCode(user.getMfaSecret(), code)) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            session.removeAttribute("PRE_AUTH_USER");

            if (!user.isMfaEnabled()) {
                user.setMfaEnabled(true);
                userRepository.save(user);
            }
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(401).body("Invalid MFA Code.");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, 30); // 30 minutes expiry

            // Delete old tokens for this user, then save new one
            tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
            tokenRepository.save(resetToken);

            String resetUrl = "http://localhost:8080/#/reset-password?token=" + token; // Adjust host as needed

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Password Reset Request");
                message.setText("To reset your password, click the link below:\n" + resetUrl);
                mailSender.send(message);

                // For dev if mail isn't actually configuring anything but a console sink, this
                // acts as a print.
                System.out.println("MOCK MAIL SENT: " + message.getText());
            } catch (Exception e) {
                System.err.println("Failed to send mail, falling back to console log. Link: " + resetUrl);
            }
        }

        // Always return OK to prevent email enumeration
        return ResponseEntity.ok("If an account with that email exists, a password reset link has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Optional<PasswordResetToken> tokenOptional = tokenRepository.findByToken(token);

        if (tokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid password reset token.");
        }

        PasswordResetToken resetToken = tokenOptional.get();
        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            return ResponseEntity.badRequest().body("Token has expired.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return ResponseEntity.ok("Password has been reset successfully.");
    }
}
