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

    List<ConversationParticipant> findByIdConversationId(Long conversationId);

    boolean existsByIdUserIdAndIdConversationId(Long userId, Long conversationId);

    @Query("""
        SELECT cp.id.conversationId
        FROM ConversationParticipant cp
        JOIN cp.conversation c
        WHERE c.type = com.example.chat.conversation.entity.ConversationType.PRIVATE
          AND cp.id.conversationId IN (
              SELECT cp1.id.conversationId FROM ConversationParticipant cp1 WHERE cp1.id.userId = :user1
          )
          AND cp.id.conversationId IN (
              SELECT cp2.id.conversationId FROM ConversationParticipant cp2 WHERE cp2.id.userId = :user2
          )
        GROUP BY cp.id.conversationId
    """)
    List<Long> findPrivateConversationBetween(@Param("user1") Long user1, @Param("user2") Long user2);

    @Query("SELECT cp.id.conversationId FROM ConversationParticipant cp WHERE cp.id.userId IN :userIds GROUP BY cp.id.conversationId HAVING COUNT(cp.id.conversationId) = :size")
    List<Long> findConversationsWithExactParticipants(@Param("userIds") List<Long> userIds, @Param("size") long size);
}
