package com.example.chat.conversation.mapper;

import com.example.chat.conversation.dto.ChatSummaryResponse;
import com.example.chat.conversation.dto.ConversationResponse;
import com.example.chat.conversation.entity.Conversation;
import com.example.chat.message.dto.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getName(),
                conversation.getAvatarUrl(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    public ChatSummaryResponse toSummary(Conversation conversation, MessageResponse lastMessage, long unreadCount) {
        return toSummary(conversation, conversation.getName(), conversation.getAvatarUrl(), lastMessage, unreadCount);
    }

    public ChatSummaryResponse toSummary(Conversation conversation, String displayName, String displayAvatarUrl, MessageResponse lastMessage, long unreadCount) {
        return new ChatSummaryResponse(
                conversation.getId(),
                conversation.getType(),
                displayName != null ? displayName : conversation.getName(),
                displayAvatarUrl != null ? displayAvatarUrl : conversation.getAvatarUrl(),
                lastMessage,
                unreadCount,
                conversation.getUpdatedAt()
        );
    }
}
