package edu.greenchannel.attachment;

import java.time.LocalDateTime;

public record AttachmentRecord(
        long id,
        long ownerId,
        String originalName,
        String storedName,
        String contentType,
        long size,
        String storagePath,
        String businessType,
        Long businessId,
        String status,
        LocalDateTime createdTime
) {
}
