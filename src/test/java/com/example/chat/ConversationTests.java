package com.example.chat;

import com.example.chat.conversation.entity.Conversation;
import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.conversation.repository.ConversationParticipantRepository;
import com.example.chat.conversation.repository.ConversationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ConversationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Test
    void staticGroup_shouldContainFiveUsers() {
        Conversation systemChat = conversationRepository.findByName("System Chat")
                .orElseThrow(() -> new AssertionError("System Chat not seeded"));

        List<ConversationParticipant> participants = participantRepository.findByIdConversationId(systemChat.getId());
        assertThat(participants).hasSize(5);
    }

    @Test
    void createPrivateChat_andReuseExisting_withoutDuplicates() throws Exception {
        // Create or get chat between Ali (id=3) and Omar (id=5)
        String response1 = mockMvc.perform(post("/api/chats/private")
                        .header("X-User-Id", "3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("PRIVATE"))
                .andReturn().getResponse().getContentAsString();

        // Perform the exact same request again from the other direction
        String response2 = mockMvc.perform(post("/api/chats/private")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("PRIVATE"))
                .andReturn().getResponse().getContentAsString();

        // Both responses should return the exact same conversation ID
        assertThat(response1).isEqualTo(response2);
    }

    @Test
    void createPrivateChat_withSelf_shouldFail() throws Exception {
        mockMvc.perform(post("/api/chats/private")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void listChats_shouldReturnUserConversations() throws Exception {
        mockMvc.perform(get("/api/chats")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].lastMessage").exists());
    }
}
