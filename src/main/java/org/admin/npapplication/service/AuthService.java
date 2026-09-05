package org.admin.npapplication.service;

import jakarta.servlet.http.HttpServletResponse;
import org.admin.npapplication.dto.ApiResponse;
import org.admin.npapplication.dto.LoginRequest;
import org.admin.npapplication.dto.LoginResponse;
import org.admin.npapplication.dto.RegisterRequest;
import org.admin.npapplication.dto.UserResponse;
import org.admin.npapplication.model.User;
import org.admin.npapplication.repository.UserRepository;
import org.admin.npapplication.security.AuthCookieService;
import org.admin.npapplication.security.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthCookieService authCookieService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtTokenProvider tokenProvider,
            PasswordEncoder passwordEncoder,
            AuthCookieService authCookieService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authCookieService = authCookieService;
    }

    @Transactional
    public ApiResponse registerUser(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User newUser = new User();
        newUser.setFullname(request.getFullname().trim());
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        // Public registration must never create an administrator account.
        newUser.setRole("ROLE_USER");

        userRepository.save(newUser);
        return new ApiResponse("Account created successfully!");
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        return authenticateAndCreateSession(request, response, false);
    }

    public LoginResponse loginAdmin(LoginRequest request, HttpServletResponse response) {
        return authenticateAndCreateSession(request, response, true);
    }

    private LoginResponse authenticateAndCreateSession(
            LoginRequest request,
            HttpServletResponse response,
            boolean adminOnly
    ) {
        String email = normalizeEmail(request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        String role = user.getRole() == null ? "ROLE_USER" : user.getRole();

        if (adminOnly && !"ROLE_ADMIN".equals(role)) {
            throw new BadCredentialsException("Invalid admin credentials");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(user.getEmail(), role);
        authCookieService.addAuthenticationCookie(response, token);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");

        return new LoginResponse("Login successful", toUserResponse(user, role));
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        User user;
        if (authentication.getPrincipal() instanceof User authenticatedUser) {
            user = authenticatedUser;
        } else {
            user = userRepository.findByEmailIgnoreCase(authentication.getName())
                    .orElse(null);
        }

        if (user == null) {
            return null;
        }

        String role = user.getRole() == null ? "ROLE_USER" : user.getRole();
        return toUserResponse(user, role);
    }

    public void logout(HttpServletResponse response) {
        authCookieService.clearAuthenticationCookie(response);
        SecurityContextHolder.clearContext();
    }

    private UserResponse toUserResponse(User user, String role) {
        return new UserResponse(user.getId(), user.getFullname(), user.getEmail(), role);
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT).trim();
    }
}
