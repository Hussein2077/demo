package com.example.chat.attachment.repository;

import com.example.chat.attachment.entity.MessageAttachment;
import java.util.List;

public interface MessageAttachmentRepository {
    MessageAttachment save(MessageAttachment attachment);
    List<MessageAttachment> findByMessageIdIn(List<Long> messageIds);
    List<MessageAttachment> findByMessageId(Long messageId);
}
