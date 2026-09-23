package com.example.chat.conversation.repository;

import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.conversation.entity.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {
    
    List<ConversationParticipant> findByIdUserId(Long userId);

    @Query("SELECT cp.id.conversationId FROM ConversationParticipant cp WHERE cp.id.userId IN :userIds GROUP BY cp.id.conversationId HAVING COUNT(cp.id.conversationId) = :size")
    List<Long> findConversationsWithExactParticipants(@Param("userIds") List<Long> userIds, @Param("size") long size);

    boolean existsByIdUserIdAndIdConversationId(Long userId, Long conversationId);
}
