package com.example.chat.conversation.dto;

import com.example.chat.conversation.entity.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private ConversationType type;
    private String name;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
