package com.example.chat.message.dto;

import com.example.chat.attachment.dto.AttachmentResponse;
import com.example.chat.message.entity.MessageType;
import com.example.chat.user.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private Long conversationId;
    private UserResponse sender;
    private MessageType type;
    private String content;
    private List<AttachmentResponse> attachments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
