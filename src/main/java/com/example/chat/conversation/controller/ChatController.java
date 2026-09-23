package com.example.chat.conversation.controller;

import com.example.chat.common.response.ApiResponse;
import com.example.chat.config.CurrentUserProvider;
import com.example.chat.conversation.dto.ChatSummaryResponse;
import com.example.chat.conversation.dto.ConversationResponse;
import com.example.chat.conversation.dto.CreatePrivateChatRequest;
import com.example.chat.conversation.dto.MarkReadRequest;
import com.example.chat.conversation.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Chats")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserProvider currentUserProvider;

    public ChatController(ChatService chatService, CurrentUserProvider currentUserProvider) {
        this.chatService = chatService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    @Operation(summary = "List all chats for the current user")
    public ResponseEntity<ApiResponse<List<ChatSummaryResponse>>> listChats() {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        List<ChatSummaryResponse> chats = chatService.listChats(currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(chats));
    }

    @PostMapping("/private")
    @Operation(summary = "Get or create a private conversation with another user")
    public ResponseEntity<ApiResponse<ConversationResponse>> getOrCreatePrivateChat(
            @Valid @RequestBody CreatePrivateChatRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        ConversationResponse response = chatService.getOrCreatePrivateChat(currentUserId, request.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{chatId}/read")
    @Operation(summary = "Mark conversation as read up to a message ID")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long chatId,
            @Valid @RequestBody MarkReadRequest request) {
        Long currentUserId = currentUserProvider.getCurrentUserId();
        chatService.assertParticipant(currentUserId, chatId);
        chatService.markRead(chatId, currentUserId, request.getMessageId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
