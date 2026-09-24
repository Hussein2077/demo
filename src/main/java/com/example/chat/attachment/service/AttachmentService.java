package com.example.chat.attachment.service;

import com.example.chat.attachment.entity.MessageAttachment;
import com.example.chat.attachment.repository.MessageAttachmentRepository;
import com.example.chat.attachment.storage.FileStorageService;
import com.example.chat.attachment.storage.StoredFile;
import com.example.chat.common.exception.FileTooLargeException;
import com.example.chat.common.exception.ResourceNotFoundException;
import com.example.chat.conversation.service.ChatService;
import com.example.chat.message.dto.MessageResponse;
import com.example.chat.message.entity.Message;
import com.example.chat.message.entity.MessageType;
import com.example.chat.message.service.MessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AttachmentService {

    private final FileStorageService fileStorageService;
    private final MessageAttachmentRepository attachmentRepository;
    private final MessageService messageService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${chat.files.max-size}")
    private long maxFileSize;

    public AttachmentService(FileStorageService fileStorageService,
                             MessageAttachmentRepository attachmentRepository,
                             MessageService messageService,
                             ChatService chatService,
                             SimpMessagingTemplate messagingTemplate) {
        this.fileStorageService = fileStorageService;
        this.attachmentRepository = attachmentRepository;
        this.messageService = messageService;
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    public MessageResponse uploadFileMessage(Long chatId, Long currentUserId, MultipartFile file, String content) {
        chatService.assertParticipant(currentUserId, chatId);
        validateFile(file);

        Message message = messageService.createMessage(chatId, currentUserId, MessageType.FILE, content);
        StoredFile stored = fileStorageService.store(file);

        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessageId(message.getId());
        attachment.setFileName(stored.getOriginalFileName());
        attachment.setContentType(stored.getContentType());
        attachment.setFileSize(stored.getFileSize());
        attachment.setStorageKey(stored.getStorageKey());
        attachmentRepository.save(attachment);

        MessageResponse response = messageService.buildResponse(message);
        messageService.broadcastMessageEvent(chatId, response, message.getCreatedAt());

        return response;
    }

    public AttachmentDownload getFileForUser(Long attachmentId, Long currentUserId) {
        MessageAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found: " + attachmentId));

        Message message = messageService.findById(attachment.getMessageId());
        chatService.assertParticipant(currentUserId, message.getConversationId());

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
