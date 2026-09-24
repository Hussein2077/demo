package com.example.chat.message.dto;

import com.example.chat.message.entity.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SendMessageRequest {
    @NotNull
    private MessageType type;
    private String content;
}
