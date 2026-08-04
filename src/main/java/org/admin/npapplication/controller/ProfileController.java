package org.admin.npapplication.controller;

import org.admin.npapplication.dto.*;
import org.admin.npapplication.model.User;
import org.admin.npapplication.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal User user) {
        UserResponse profile = new UserResponse(
                user.getId(),
                user.getFullname(),
                user.getEmail(),
                user.getRole()
        );
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse updated = profileService.updateProfile(user, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        profileService.changePassword(user, request);
        return ResponseEntity.ok(new ApiResponse("Password changed successfully"));
    }
}