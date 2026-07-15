package edu.greenchannel.message;

import java.time.LocalDateTime;

public record MessageRecord(
        long id,
        long receiverUserId,
        String eventCode,
        String businessId,
        String title,
        String content,
        String messageType,
        boolean read,
        LocalDateTime readTime,
        LocalDateTime createdTime) {
}
