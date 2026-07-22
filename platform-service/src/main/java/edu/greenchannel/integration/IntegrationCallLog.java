package edu.greenchannel.integration;

import java.time.LocalDateTime;

public record IntegrationCallLog(
        long id,
        String clientId,
        String requestMethod,
        String requestPath,
        boolean success,
        long durationMs,
        String failureType,
        LocalDateTime createdTime) {
}
