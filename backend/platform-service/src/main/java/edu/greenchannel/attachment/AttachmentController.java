package edu.greenchannel.attachment;

import edu.greenchannel.auth.AuthInterceptor;
import edu.greenchannel.auth.CurrentUser;
import edu.greenchannel.common.ApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {
    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AttachmentMetadata> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Long businessId,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(service.upload(file, businessType, businessId, user));
    }

    @GetMapping("/{id}")
    public ApiResponse<AttachmentMetadata> metadata(
            @PathVariable long id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(service.metadata(id, user));
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<Resource> content(
            @PathVariable long id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        DownloadableAttachment attachment = service.content(id, user);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.metadata().originalName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.metadata().contentType()))
                .contentLength(attachment.metadata().size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(attachment.resource());
    }

    @PutMapping("/{id}/binding")
    public ApiResponse<AttachmentMetadata> bind(
            @PathVariable long id,
            @RequestBody AttachmentBindingRequest request,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        return ApiResponse.success(service.bind(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable long id,
            @RequestAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE) CurrentUser user) {
        service.delete(id, user);
        return ApiResponse.success(null);
    }
}
