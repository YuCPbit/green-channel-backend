package edu.greenchannel.message;

import java.util.Map;

public record MessageEvent(
        String eventType,
        long receiverUserId,
        String businessId,
        Map<String, Object> variables) {
}
