package com.example.chat.conversation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePrivateChatRequest {
    @NotNull(message = "userId is required")
    private Long userId;
}
