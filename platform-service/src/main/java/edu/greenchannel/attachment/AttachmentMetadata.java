package edu.greenchannel.attachment;

import java.time.LocalDateTime;

public record AttachmentMetadata(
        long id,
        String originalName,
        String contentType,
        long size,
        String businessType,
        Long businessId,
        String status,
        LocalDateTime createdTime
) {
    static AttachmentMetadata from(AttachmentRecord record) {
        return new AttachmentMetadata(
                record.id(), record.originalName(), record.contentType(), record.size(),
                record.businessType(), record.businessId(), record.status(), record.createdTime()
        );
    }
}
