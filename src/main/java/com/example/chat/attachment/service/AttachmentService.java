package com.example.chat.attachment.service;

import com.example.chat.attachment.entity.MessageAttachment;
import com.example.chat.attachment.repository.MessageAttachmentRepository;
import com.example.chat.attachment.storage.FileStorageService;
import com.example.chat.attachment.storage.StoredFile;
import com.example.chat.common.exception.FileTooLargeException;
import com.example.chat.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

    private final FileStorageService fileStorageService;
    private final MessageAttachmentRepository attachmentRepository;

    @Value("${chat.files.max-size}")
    private long maxFileSize;

    public AttachmentService(FileStorageService fileStorageService,
                             MessageAttachmentRepository attachmentRepository) {
        this.fileStorageService = fileStorageService;
        this.attachmentRepository = attachmentRepository;
    }

    @Transactional
    public MessageAttachment storeAndSave(MultipartFile file, Long messageId) {
        validateFile(file);

        StoredFile stored = fileStorageService.store(file);

        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessageId(messageId);
        attachment.setFileName(stored.getOriginalFileName());
        attachment.setContentType(stored.getContentType());
        attachment.setFileSize(stored.getFileSize());
        attachment.setStorageKey(stored.getStorageKey());

        return attachmentRepository.save(attachment);
    }

    @Transactional(readOnly = true)
    public AttachmentDownload resolveDownload(Long attachmentId) {
        MessageAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));
        Resource resource = fileStorageService.load(attachment.getStorageKey());
        return new AttachmentDownload(attachment, resource);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new IllegalArgumentException("File must have a valid filename");
        }
        if (file.getSize() > maxFileSize) {
            throw new FileTooLargeException("File size exceeds the maximum allowed limit of " + (maxFileSize / 1024 / 1024) + " MB");
        }
        if (file.getContentType() == null || file.getContentType().isBlank()) {
            throw new IllegalArgumentException("File must have a valid content type");
        }
    }

    public record AttachmentDownload(MessageAttachment attachment, Resource resource) {}
}
