package edu.greenchannel.attachment;

import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class AttachmentService {
    static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "png", "jpg", "jpeg", "xlsx", "docx");
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "pdf", "application/pdf",
            "png", "image/png",
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final AttachmentRepository repository;
    private final Path storageRoot;

    public AttachmentService(AttachmentRepository repository,
                             @Value("${app.attachment.storage-path:./data/attachments}") String storagePath) {
        this.repository = repository;
        this.storageRoot = Path.of(storagePath).toAbsolutePath().normalize();
    }

    public AttachmentMetadata upload(MultipartFile file, String businessType, Long businessId, CurrentUser user) {
        byte[] content = readAndValidate(file);
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String storedName = UUID.randomUUID() + "." + extension;
        Path target = storageRoot.resolve(storedName).normalize();
        if (!target.startsWith(storageRoot)) {
            throw new BusinessException(40001, "附件名称不合法");
        }
        try {
            Files.createDirectories(storageRoot);
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
        } catch (IOException exception) {
            throw new BusinessException(50000, "附件保存失败");
        }

        AttachmentRecord draft = new AttachmentRecord(
                0, user.id(), originalName, storedName, CONTENT_TYPES.get(extension), content.length,
                target.toString(), normalizeBusinessType(businessType), businessId, "READY", LocalDateTime.now()
        );
        try {
            return AttachmentMetadata.from(repository.save(draft));
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
                // Database failure remains the primary error.
            }
            throw exception;
        }
    }

    public AttachmentMetadata metadata(long id, CurrentUser user) {
        return AttachmentMetadata.from(requireAccessible(id, user));
    }

    public DownloadableAttachment content(long id, CurrentUser user) {
        AttachmentRecord record = requireAccessible(id, user);
        Path path = Path.of(record.storagePath()).toAbsolutePath().normalize();
        if (!path.startsWith(storageRoot) || !Files.isRegularFile(path)) {
            throw new BusinessException(40400, "附件内容不存在");
        }
        return new DownloadableAttachment(record, new FileSystemResource(path));
    }

    public AttachmentMetadata bind(long id, AttachmentBindingRequest request, CurrentUser user) {
        AttachmentRecord record = requireOwner(id, user);
        if (request == null || request.businessId() == null || request.businessId() <= 0) {
            throw new BusinessException(40001, "businessId 必须为正整数");
        }
        String businessType = normalizeBusinessType(request.businessType());
        repository.bind(record.id(), businessType, request.businessId());
        return AttachmentMetadata.from(new AttachmentRecord(
                record.id(), record.ownerId(), record.originalName(), record.storedName(), record.contentType(),
                record.size(), record.storagePath(), businessType, request.businessId(), record.status(), record.createdTime()
        ));
    }

    public void delete(long id, CurrentUser user) {
        AttachmentRecord record = requireOwner(id, user);
        if (record.businessId() != null) {
            throw new BusinessException(40900, "已绑定业务的附件不能直接删除");
        }
        repository.softDelete(id);
        try {
            Files.deleteIfExists(Path.of(record.storagePath()));
        } catch (IOException exception) {
            throw new BusinessException(50000, "附件文件删除失败");
        }
    }

    private AttachmentRecord requireAccessible(long id, CurrentUser user) {
        AttachmentRecord record = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "附件不存在"));
        if (record.ownerId() != user.id() && !user.roles().contains("SYSTEM_ADMIN")) {
            throw new BusinessException(40300, "无权访问该附件");
        }
        return record;
    }

    private AttachmentRecord requireOwner(long id, CurrentUser user) {
        AttachmentRecord record = repository.findById(id)
                .orElseThrow(() -> new BusinessException(40400, "附件不存在"));
        if (record.ownerId() != user.id()) {
            throw new BusinessException(40300, "只有上传者可以修改附件");
        }
        return record;
    }

    private byte[] readAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40001, "请选择附件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(40001, "单个附件不能超过 20 MB");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(40001, "不支持的附件类型");
        }
        try {
            byte[] content = file.getBytes();
            if (!matchesSignature(extension, content)) {
                throw new BusinessException(40001, "附件内容与扩展名不匹配");
            }
            return content;
        } catch (IOException exception) {
            throw new BusinessException(40001, "附件读取失败");
        }
    }

    private boolean matchesSignature(String extension, byte[] bytes) {
        return switch (extension) {
            case "pdf" -> startsWith(bytes, 0x25, 0x50, 0x44, 0x46);
            case "png" -> startsWith(bytes, 0x89, 0x50, 0x4E, 0x47);
            case "jpg", "jpeg" -> startsWith(bytes, 0xFF, 0xD8, 0xFF);
            case "xlsx", "docx" -> startsWith(bytes, 0x50, 0x4B);
            default -> false;
        };
    }

    private boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int index = 0; index < signature.length; index++) {
            if ((bytes[index] & 0xFF) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private String extensionOf(String name) {
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeBusinessType(String businessType) {
        if (!StringUtils.hasText(businessType)) {
            return null;
        }
        String normalized = businessType.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z][A-Z0-9_]{1,49}")) {
            throw new BusinessException(40001, "businessType 格式不正确");
        }
        return normalized;
    }
}
