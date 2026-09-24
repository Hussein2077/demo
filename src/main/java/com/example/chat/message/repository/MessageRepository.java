package com.example.chat.message.repository;

import com.example.chat.message.entity.Message;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);
    Optional<Message> findById(Long id);
    List<Message> findLatestByConversationId(Long conversationId, Pageable pageable);
    List<Message> findByConversationIdAndIdBeforeOrderByIdDesc(Long conversationId, Long beforeId, Pageable pageable);
    Optional<Message> findFirstByConversationIdOrderByIdDesc(Long conversationId);
    long countByConversationId(Long conversationId);
    long countUnreadMessages(Long conversationId, Long lastReadMessageId, Long userId);
}
