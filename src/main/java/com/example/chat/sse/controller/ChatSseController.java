package com.example.chat.sse.controller;

import com.example.chat.sse.service.ChatSseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE endpoint for delivering real‑time chat‑list updates to the Flutter client.
 */
@RestController
@RequestMapping("/api/chats")
public class ChatSseController {

    private final ChatSseService chatSseService;

    @Autowired
    public ChatSseController(ChatSseService chatSseService) {
        this.chatSseService = chatSseService;
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestHeader("X-User-Id") Long userId) {
        // TODO: validate that the user exists – for brevity we trust the header here
        return chatSseService.register(userId);
    }
}
