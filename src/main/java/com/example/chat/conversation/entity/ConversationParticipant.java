package com.example.chat.conversation.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ConversationParticipant {

    private ConversationParticipantId id = new ConversationParticipantId();

    private Conversation conversation;

    // We don't necessarily need the User entity here, but we store userId in the EmbeddedId
    
    private LocalDateTime joinedAt = LocalDateTime.now();

    private Long lastReadMessageId = 0L;

    public ConversationParticipant(Conversation conversation, Long userId) {
        this.conversation = conversation;
        this.id.setConversationId(conversation.getId());
        this.id.setUserId(userId);
    }
}
