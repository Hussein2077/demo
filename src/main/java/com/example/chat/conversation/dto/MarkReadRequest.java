package com.example.chat.conversation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MarkReadRequest {
    @NotNull(message = "messageId is required")
    private Long messageId;
}
