package com.example.chat;

import com.example.chat.conversation.entity.Conversation;
import com.example.chat.conversation.repository.ConversationParticipantRepository;
import com.example.chat.conversation.repository.ConversationRepository;
import com.example.chat.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MessageTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageService messageService;

    @Test
    void loadLatestMessages_andCursorPagination() throws Exception {
        Conversation systemChat = conversationRepository.findByName("System Chat")
                .orElseThrow(() -> new AssertionError("System Chat not found"));

        // Fetch first batch with limit 20
        String result = mockMvc.perform(get("/api/chats/{chatId}/messages", systemChat.getId())
                        .param("limit", "20")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(20)))
                .andExpect(jsonPath("$.pagination.hasMore").value(true))
                .andExpect(jsonPath("$.pagination.nextCursor").isNumber())
                .andReturn().getResponse().getContentAsString();

        // Extract nextCursor
        com.fasterxml.jackson.databind.JsonNode rootNode = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result);
        long nextCursor = rootNode.path("pagination").path("nextCursor").asLong();

        // Fetch older messages using cursor
        mockMvc.perform(get("/api/chats/{chatId}/messages", systemChat.getId())
                        .param("limit", "20")
                        .param("before", String.valueOf(nextCursor))
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(20)))
                .andExpect(jsonPath("$.data[19].id", lessThan((int) nextCursor)));
    }

    @Test
    void authorization_nonParticipant_shouldReturnForbidden() throws Exception {
        // Find private chat between Ahmed (1) and Sara (4)
        Long privateChatId = participantRepository.findPrivateConversationBetween(1L, 4L).get(0);

        // Omar (5) is not a participant, should be rejected with 403 Forbidden
        mockMvc.perform(get("/api/chats/{chatId}/messages", privateChatId)
                        .header("X-User-Id", "5"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
