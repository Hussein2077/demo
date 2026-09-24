package com.example.chat.websocket.controller;

import com.example.chat.message.dto.ReadMessageRequest;
import com.example.chat.message.dto.SendMessageRequest;
import com.example.chat.message.service.MessageService;
import com.example.chat.read.ReadStateService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final ReadStateService readStateService;

    public ChatWebSocketController(MessageService messageService, ReadStateService readStateService) {
        this.messageService = messageService;
        this.readStateService = readStateService;
    }

    @MessageMapping("/chats/{chatId}/messages")
    public void handleMessage(@DestinationVariable Long chatId,
                              @Payload SendMessageRequest request,
                              Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("User not authenticated in WebSocket session");
        }
        Long senderId = Long.parseLong(principal.getName());
        messageService.sendTextMessage(chatId, senderId, request.getContent());
    }

    @MessageMapping("/chats/{chatId}/read")
    public void handleRead(@DestinationVariable Long chatId,
                           @Payload ReadMessageRequest request,
                           Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalArgumentException("User not authenticated in WebSocket session");
        }
        Long userId = Long.parseLong(principal.getName());
        readStateService.markRead(userId, chatId, request.getMessageId());
        // Optionally broadcast read receipt via SSE (handled elsewhere)
    }
}
