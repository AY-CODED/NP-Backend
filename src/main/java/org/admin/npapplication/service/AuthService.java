package org.admin.npapplication.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.admin.npapplication.dto.*;
import org.admin.npapplication.model.User;
import org.admin.npapplication.repository.UserRepository;
import org.admin.npapplication.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.samesite:None}")
    private String cookieSameSite;

    public ApiResponse registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("An account with this email already exists.");
        }

        User newUser = new User();
        newUser.setFullname(request.getFullname());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        if (request.getEmail().endsWith("@nugespharmacy.com")) {
            newUser.setRole("ADMIN");
        } else if (newUser.getRole() == null) {
            newUser.setRole("ROLE_USER");
        }

        userRepository.save(newUser);
        return new ApiResponse("Account created successfully!");
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        return authenticateAndGenerateToken(request, response);
    }

    public LoginResponse loginAdmin(LoginRequest request, HttpServletResponse response) {
        return authenticateAndGenerateToken(request, response);
    }

    private LoginResponse authenticateAndGenerateToken(LoginRequest request, HttpServletResponse response) {
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
        if (role.startsWith("ROLE_")) {
            role = role.replace("ROLE_", "");
        }
        if (user.getEmail().endsWith("@nugespharmacy.com")) {
            role = "ADMIN";
        }

        String token = tokenProvider.generateToken(request.getEmail(), role);

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setAttribute("SameSite", cookieSameSite);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);
        response.setHeader("Authorization", "Bearer " + token);

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFullname(),
                user.getEmail(),
                role
        );

        return new LoginResponse("Login successful", userResponse, token);
    }
}