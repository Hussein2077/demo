package com.example.chat.conversation.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_participants", indexes = {
    @Index(name = "idx_participant_user_conv", columnList = "user_id, conversation_id")
})
@Getter
@Setter
@NoArgsConstructor
public class ConversationParticipant {

    @EmbeddedId
    private ConversationParticipantId id = new ConversationParticipantId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("conversationId")
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    // We don't necessarily need the User entity here, but we store userId in the EmbeddedId
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt = LocalDateTime.now();

    private Long lastReadMessageId = 0L;

    public ConversationParticipant(Conversation conversation, Long userId) {
        this.conversation = conversation;
        this.id.setConversationId(conversation.getId());
        this.id.setUserId(userId);
    }
}
