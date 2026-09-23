package com.example.chat.conversation.dto;

import com.example.chat.conversation.entity.ConversationType;
import com.example.chat.message.dto.MessageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatSummaryResponse {
    private Long id;
    private ConversationType type;
    private String name;
    private String avatarUrl;
    private MessageResponse lastMessage;
    private long unreadCount;
    private LocalDateTime updatedAt;
}
