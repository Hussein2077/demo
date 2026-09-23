package com.example.chat.websocket.controller;

import com.example.chat.message.dto.SendMessageRequest;
import com.example.chat.message.service.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;

    public ChatWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chats/{chatId}/messages")
    public void handleMessage(@DestinationVariable Long chatId,
                              @Payload SendMessageRequest request,
                              @Header(name = "X-User-Id", required = false) String userIdHeader,
                              SimpMessageHeaderAccessor headerAccessor) {
        Long senderId = extractUserId(userIdHeader, headerAccessor);
        messageService.sendTextMessage(chatId, senderId, request.getContent());
    }

    private Long extractUserId(String userIdHeader, SimpMessageHeaderAccessor headerAccessor) {
        String value = userIdHeader;
        if (value == null || value.isBlank()) {
            value = headerAccessor.getFirstNativeHeader("X-User-Id");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("X-User-Id header is required for WebSocket messages");
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid user ID in X-User-Id header: " + value);
        }
    }
}
