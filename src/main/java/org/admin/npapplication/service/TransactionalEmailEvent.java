package org.admin.npapplication.service;

public record TransactionalEmailEvent(
        String recipient,
        String subject,
        String body,
        String category
) {}
