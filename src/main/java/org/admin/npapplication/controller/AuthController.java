package org.admin.npapplication.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.admin.npapplication.dto.ApiResponse;
import org.admin.npapplication.dto.LoginRequest;
import org.admin.npapplication.dto.LoginResponse;
import org.admin.npapplication.dto.RegisterRequest;
import org.admin.npapplication.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = authService.login(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(
            @RequestBody RegisterRequest request
    ) {
        try {
            ApiResponse res = authService.registerUser(request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ApiResponse(e.getMessage()));
        }
    }
}