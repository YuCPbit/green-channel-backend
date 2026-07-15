package edu.greenchannel.message;

import java.util.List;

public record MessageTemplate(
        long id,
        String eventCode,
        String titleTemplate,
        String contentTemplate,
        String messageType,
        List<String> channels) {
}
