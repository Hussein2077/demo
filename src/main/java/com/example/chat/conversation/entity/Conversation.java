package com.example.chat.conversation.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Conversation {

    private Long id;

    private ConversationType type;

    private String name;

    private String avatarUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
    
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
