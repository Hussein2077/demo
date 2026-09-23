package com.example.chat.attachment.repository;

import com.example.chat.attachment.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
    List<MessageAttachment> findByMessageIdIn(List<Long> messageIds);
    List<MessageAttachment> findByMessageId(Long messageId);
}
