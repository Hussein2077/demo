package com.example.chat.message.controller;

import com.example.chat.common.response.ApiResponse;
import com.example.chat.common.response.PaginatedResponse;
import com.example.chat.config.CurrentUserProvider;
import com.example.chat.conversation.service.ChatService;
import com.example.chat.message.dto.MessageResponse;
import com.example.chat.message.dto.SendMessageRequest;
import com.example.chat.message.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats/{chatId}/messages")
@Tag(name = "Messages")
public class MessageController {

    private final MessageService messageService;
    private final ChatService chatService;
    private final CurrentUserProvider currentUserProvider;

    public MessageController(MessageService messageService,
                             ChatService chatService,
                             CurrentUserProvider currentUserProvider) {
        this.messageService = messageService;
        this.chatService = chatService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @Operation(summary = "Get message history with cursor-based pagination")
    public ResponseEntity<PaginatedResponse<MessageResponse>> getHistory(
            @PathVariable Long chatId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) Long before) {

        Long currentUserId = currentUserProvider.getCurrentUserId();
        chatService.assertParticipant(currentUserId, chatId);

        List<MessageResponse> messages = messageService.getHistory(chatId, limit, before);

        boolean hasMore = messages.size() == limit;
        Long nextCursor = hasMore ? messages.get(0).getId() : null;

        return ResponseEntity.ok(PaginatedResponse.of(messages, hasMore, nextCursor));
    }

    @PostMapping
    @Operation(summary = "Send a text message")
    public ResponseEntity<ApiResponse<MessageResponse>> sendText(
            @PathVariable Long chatId,
            @Valid @RequestBody SendMessageRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        MessageResponse response = messageService.sendTextMessage(chatId, currentUserId, request.getContent());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
