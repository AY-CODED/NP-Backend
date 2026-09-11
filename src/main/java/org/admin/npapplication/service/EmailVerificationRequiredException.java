package org.admin.npapplication.service;

public class EmailVerificationRequiredException extends RuntimeException {
    public EmailVerificationRequiredException() {
        super("Verify your email before signing in");
    }
}
