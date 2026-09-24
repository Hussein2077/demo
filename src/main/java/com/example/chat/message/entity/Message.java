package com.example.chat.message.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Message {

    private Long id;

    private Long conversationId;

    private Long senderId;

    private MessageType type;

    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime deletedAt;

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
