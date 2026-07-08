package org.admin.npapplication.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.admin.npapplication.dto.LoginRequest;
import org.admin.npapplication.dto.RegisterRequest;
import org.admin.npapplication.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        System.out.println("LOGIN ENDPOINT HIT");
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.registerUser(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
