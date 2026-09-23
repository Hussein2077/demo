package com.example.chat.message.dto;

import com.example.chat.message.entity.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendMessageRequest {
    @NotNull
    private MessageType type;
    private String content;
}
