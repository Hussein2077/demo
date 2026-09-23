package com.example.chat;

import com.example.chat.attachment.entity.MessageAttachment;
import com.example.chat.attachment.repository.MessageAttachmentRepository;
import com.example.chat.conversation.repository.ConversationParticipantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageAttachmentRepository attachmentRepository;

    @Test
    void uploadAndDownload_withAuthorization() throws Exception {
        // Chat between Ahmed (1) and Mohamed (2)
        Long chatId = participantRepository.findPrivateConversationBetween(1L, 2L).get(0);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample-contract.pdf",
                "application/pdf",
                "Sample PDF content for chat testing".getBytes()
        );

        // 1. Ahmed (1) uploads file
        String uploadResponse = mockMvc.perform(multipart("/api/chats/{chatId}/messages/files", chatId)
                        .file(file)
                        .param("content", "Here is the contract")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.type").value("FILE"))
                .andExpect(jsonPath("$.data.attachments", hasSize(1)))
                .andExpect(jsonPath("$.data.attachments[0].fileName").value("sample-contract.pdf"))
                .andReturn().getResponse().getContentAsString();

        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(uploadResponse);
        long attachmentId = node.path("data").path("attachments").get(0).path("id").asLong();

        // 2. Mohamed (2) downloads file (participant in chat -> 200 OK)
        mockMvc.perform(get("/api/files/{attachmentId}", attachmentId)
                        .header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("sample-contract.pdf")));

        // 3. Sara (4) tries to download file (not participant in this chat -> 403 Forbidden)
        mockMvc.perform(get("/api/files/{attachmentId}", attachmentId)
                        .header("X-User-Id", "4"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void upload_fileExceedingMaxSize_shouldBeRejected() throws Exception {
        Long chatId = participantRepository.findPrivateConversationBetween(1L, 2L).get(0);

        // Create a dummy byte array exceeding 10MB (10 * 1024 * 1024 + 1024 bytes)
        byte[] oversizedBytes = new byte[10 * 1024 * 1024 + 1024];

        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-video.mp4",
                "video/mp4",
                oversizedBytes
        );

        mockMvc.perform(multipart("/api/chats/{chatId}/messages/files", chatId)
                        .file(largeFile)
                        .header("X-User-Id", "1"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.success").value(false));
    }
}
