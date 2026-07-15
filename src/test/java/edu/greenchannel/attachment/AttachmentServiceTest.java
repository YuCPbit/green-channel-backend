package edu.greenchannel.attachment;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentServiceTest {
    @TempDir
    Path tempDir;

    private final CurrentUser owner = new CurrentUser(
            7, "student", "测试用户", 1, "学生", List.of("STUDENT"), List.of(), List.of());

    @Test
    void uploadsValidPdfAndPersistsMetadata() throws Exception {
        InMemoryRepository repository = new InMemoryRepository();
        AttachmentService service = new AttachmentService(repository, tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.pdf", "application/pdf", "%PDF-1.7 test".getBytes());

        AttachmentMetadata result = service.upload(file, "SUBSIDY_APPLY", null, owner);

        assertEquals("proof.pdf", result.originalName());
        assertEquals("application/pdf", result.contentType());
        assertEquals("SUBSIDY_APPLY", result.businessType());
        assertTrue(Files.exists(Path.of(repository.record.storagePath())));
    }

    @Test
    void rejectsExtensionThatDoesNotMatchContent() {
        AttachmentService service = new AttachmentService(new InMemoryRepository(), tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.pdf", "application/pdf", "not a pdf".getBytes());

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.upload(file, null, null, owner));

        assertEquals(40001, error.getCode());
    }

    @Test
    void refusesToDeleteBoundAttachment() {
        InMemoryRepository repository = new InMemoryRepository();
        repository.record = new AttachmentRecord(
                1, owner.id(), "proof.pdf", "stored.pdf", "application/pdf", 10,
                tempDir.resolve("stored.pdf").toString(), "SUBSIDY_APPLY", 99L, "READY", LocalDateTime.now());
        AttachmentService service = new AttachmentService(repository, tempDir.toString());

        BusinessException error = assertThrows(BusinessException.class, () -> service.delete(1, owner));

        assertEquals(40900, error.getCode());
        assertFalse(repository.deleted);
    }

    private static class InMemoryRepository implements AttachmentRepository {
        private AttachmentRecord record;
        private boolean deleted;

        @Override
        public AttachmentRecord save(AttachmentRecord value) {
            record = new AttachmentRecord(
                    1, value.ownerId(), value.originalName(), value.storedName(), value.contentType(),
                    value.size(), value.storagePath(), value.businessType(), value.businessId(),
                    value.status(), value.createdTime());
            return record;
        }

        @Override
        public Optional<AttachmentRecord> findById(long id) {
            return Optional.ofNullable(record);
        }

        @Override
        public void bind(long id, String businessType, long businessId) {
            record = new AttachmentRecord(
                    record.id(), record.ownerId(), record.originalName(), record.storedName(), record.contentType(),
                    record.size(), record.storagePath(), businessType, businessId, record.status(), record.createdTime());
        }

        @Override
        public void softDelete(long id) {
            deleted = true;
        }
    }
}
