package com.example.chat.conversation.repository;

import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.conversation.entity.ConversationParticipantId;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository {

    ConversationParticipant save(ConversationParticipant participant);

    Optional<ConversationParticipant> findById(ConversationParticipantId id);

    List<ConversationParticipant> findByIdUserId(Long userId);

    List<ConversationParticipant> findByIdConversationId(Long conversationId);

    boolean existsByIdUserIdAndIdConversationId(Long userId, Long conversationId);

    List<Long> findPrivateConversationBetween(Long user1, Long user2);

    List<Long> findConversationsWithExactParticipants(List<Long> userIds, long size);
}
