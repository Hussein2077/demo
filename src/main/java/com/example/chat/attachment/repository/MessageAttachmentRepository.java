package com.example.chat.attachment.repository;

import com.example.chat.attachment.entity.MessageAttachment;

import java.util.List;
import java.util.Optional;

public interface MessageAttachmentRepository {
    MessageAttachment save(MessageAttachment attachment);
    Optional<MessageAttachment> findById(Long id);
    List<MessageAttachment> findByMessageIdIn(List<Long> messageIds);
    List<MessageAttachment> findByMessageId(Long messageId);
}
