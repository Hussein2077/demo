package com.example.chat.message.repository;

import com.example.chat.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId ORDER BY m.id DESC")
    List<Message> findLatestByConversationId(@Param("conversationId") Long conversationId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.id < :beforeId ORDER BY m.id DESC")
    List<Message> findByConversationIdAndIdBeforeOrderByIdDesc(@Param("conversationId") Long conversationId, @Param("beforeId") Long beforeId, Pageable pageable);

    Optional<Message> findFirstByConversationIdOrderByIdDesc(Long conversationId);

    long countByConversationId(Long conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.id > :lastReadMessageId AND m.senderId != :userId")
    long countUnreadMessages(@Param("conversationId") Long conversationId, @Param("lastReadMessageId") Long lastReadMessageId, @Param("userId") Long userId);
}
