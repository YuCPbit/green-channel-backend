package edu.greenchannel.attachment;

import org.springframework.core.io.Resource;

public record DownloadableAttachment(AttachmentRecord metadata, Resource resource) {
}
