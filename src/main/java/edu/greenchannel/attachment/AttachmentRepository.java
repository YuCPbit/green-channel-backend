package edu.greenchannel.attachment;

import java.util.Optional;

public interface AttachmentRepository {
    AttachmentRecord save(AttachmentRecord record);

    Optional<AttachmentRecord> findById(long id);

    void bind(long id, String businessType, long businessId);

    void softDelete(long id);
}
