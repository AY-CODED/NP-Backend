package org.admin.npapplication.dto;

public record RegistrationResponse(
        String message,
        boolean emailVerificationRequired
) {}
