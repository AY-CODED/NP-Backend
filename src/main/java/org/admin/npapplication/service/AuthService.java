package org.admin.npapplication.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.admin.npapplication.dto.*;
import org.admin.npapplication.model.User;
import org.admin.npapplication.repository.UserRepository;
import org.admin.npapplication.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ApiResponse registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }

        User newUser = new User();
        newUser.setFullname(request.getFullname());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // default role if null
        if (newUser.getRole() == null) {
            newUser.setRole("ROLE_USER");
        }

        userRepository.save(newUser);
        return new ApiResponse("Account created successfully!");
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = user.getRole() != null ? user.getRole() : "ROLE_USER";
        String token = tokenProvider.generateToken(request.getEmail(), role);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        // For local development over HTTP:
        cookie.setSecure(false);      // set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

        response.addCookie(cookie);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFullname(),
                user.getEmail(),
                role
        );

        return new LoginResponse("Login successful", userResponse);
    }
}