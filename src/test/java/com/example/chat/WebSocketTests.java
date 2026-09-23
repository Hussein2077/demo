package com.example.chat;

import com.example.chat.conversation.repository.ConversationParticipantRepository;
import com.example.chat.message.dto.MessageResponse;
import com.example.chat.message.dto.SendMessageRequest;
import com.example.chat.message.entity.Message;
import com.example.chat.message.entity.MessageType;
import com.example.chat.message.repository.MessageRepository;
import com.example.chat.websocket.controller.ChatWebSocketController;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class WebSocketTests {

    @Autowired
    private ChatWebSocketController webSocketController;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void sendWebSocketMessage_shouldSaveToDatabase_andBroadcastToTopic() {
        // Chat between Ahmed (1) and Mohamed (2)
        Long chatId = participantRepository.findPrivateConversationBetween(1L, 2L).get(0);

        SendMessageRequest request = new SendMessageRequest();
        ReflectionTestUtils.setField(request, "type", MessageType.TEXT);
        ReflectionTestUtils.setField(request, "content", "Realtime WebSocket Message Test");

        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setNativeHeader("X-User-Id", "1");

        // Execute message handling
        webSocketController.handleMessage(chatId, request, "1", accessor);

        // 1. Verify saved in database
        Message latest = messageRepository.findFirstByConversationIdOrderByIdDesc(chatId)
                .orElseThrow(() -> new AssertionError("Message was not persisted"));

        assertThat(latest.getContent()).isEqualTo("Realtime WebSocket Message Test");
        assertThat(latest.getSenderId()).isEqualTo(1L);
        assertThat(latest.getConversationId()).isEqualTo(chatId);

        // 2. Verify broadcasted via SimpMessagingTemplate to /topic/chats/{chatId}
        ArgumentCaptor<MessageResponse> responseCaptor = ArgumentCaptor.forClass(MessageResponse.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/chats/" + chatId), responseCaptor.capture());

        MessageResponse broadcasted = responseCaptor.getValue();
        assertThat(broadcasted.getId()).isEqualTo(latest.getId());
        assertThat(broadcasted.getContent()).isEqualTo("Realtime WebSocket Message Test");
        assertThat(broadcasted.getSender().getId()).isEqualTo(1L);
    }
}
