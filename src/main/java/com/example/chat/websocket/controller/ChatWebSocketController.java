package com.example.chat.websocket.controller;

import com.example.chat.message.dto.SendMessageRequest;
import com.example.chat.message.service.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;

    public ChatWebSocketController(MessageService messageService) {
        this.messageService = messageService;
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
}
