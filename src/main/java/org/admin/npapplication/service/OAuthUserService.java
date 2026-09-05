package org.admin.npapplication.service;

import org.admin.npapplication.model.User;
import org.admin.npapplication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class OAuthUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuthUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User findOrCreate(String email, String fullName, boolean admin) {
        String normalizedEmail = email.toLowerCase(Locale.ROOT).trim();

        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .map(existingUser -> updateExisting(existingUser, fullName, admin))
                .orElseGet(() -> createUser(normalizedEmail, fullName, admin));
    }

    private User updateExisting(User user, String fullName, boolean admin) {
        if ((user.getFullname() == null || user.getFullname().isBlank()) && fullName != null) {
            user.setFullname(fullName.trim());
        }
        if (admin) {
            user.setRole("ROLE_ADMIN");
        }
        return userRepository.save(user);
    }

    private User createUser(String email, String fullName, boolean admin) {
        User user = new User();
        user.setEmail(email);
        user.setFullname(fullName == null || fullName.isBlank() ? email : fullName.trim());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(admin ? "ROLE_ADMIN" : "ROLE_USER");
        return userRepository.save(user);
    }
}
