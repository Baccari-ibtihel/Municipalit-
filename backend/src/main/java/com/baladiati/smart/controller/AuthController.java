package com.baladiati.smart.controller;

import com.baladiati.smart.dto.AgentActivationRequest;
import com.baladiati.smart.dto.AgentRegistrationRequest;
import com.baladiati.smart.dto.AuthResponse;
import com.baladiati.smart.dto.LoginRequest;
import com.baladiati.smart.dto.RegisterRequest;
import com.baladiati.smart.entity.User;
import com.baladiati.smart.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        try {
            User user = authService.registerCitizen(signUpRequest);
            return ResponseEntity.ok("User registered successfully: " + user.getEmail());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register-agent")
    public ResponseEntity<?> registerAgent(@RequestBody AgentRegistrationRequest request) {
        try {
            String result = authService.registerAgent(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/activate-agent")
    public ResponseEntity<?> activateAgent(@RequestBody AgentActivationRequest request) {
        try {
            String result = authService.activateAgent(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse response = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("No user session found");
        }
        try {
            User user = authService.getCurrentUser(authentication.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

