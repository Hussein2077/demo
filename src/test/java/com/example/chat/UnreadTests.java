package com.example.chat;

import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.conversation.entity.ConversationParticipantId;
import com.example.chat.conversation.repository.ConversationParticipantRepository;
import com.example.chat.conversation.service.ChatService;
import com.example.chat.message.entity.Message;
import com.example.chat.message.entity.MessageType;
import com.example.chat.message.repository.MessageRepository;
import com.example.chat.message.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UnreadTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationParticipantRepository participantRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @Test
    void unreadCount_andMarkRead_excludingOwnMessages() throws Exception {
        // Chat between Ahmed (1) and Mohamed (2)
        Long chatId = participantRepository.findPrivateConversationBetween(1L, 2L).get(0);

        // Reset Ahmed's read state to latest existing message
        Message lastExisting = messageRepository.findFirstByConversationIdOrderByIdDesc(chatId).orElseThrow();
        ConversationParticipant participant = participantRepository.   
                findById(new ConversationParticipantId(chatId, 1L)).orElseThrow();
        participant.setLastReadMessageId(lastExisting.getId());
        participantRepository.save(participant);

        // Ahmed's unread should be 0 initially
        long initialUnread = messageService.countUnread(chatId, participant.getLastReadMessageId(), 1L);
        assertThat(initialUnread).isZero();

        // 1. Mohamed sends 2 messages to the chat
        Message msg1 = messageService.createMessage(chatId, 2L, MessageType.TEXT, "Hello from Mohamed 1");
        Message msg2 = messageService.createMessage(chatId, 2L, MessageType.TEXT, "Hello from Mohamed 2");

        // 2. Ahmed sends 1 message to the chat (should NOT increase Ahmed's unread count)
        Message msgOwn = messageService.createMessage(chatId, 1L, MessageType.TEXT, "Own reply from Ahmed");

        // Ahmed's unread count should be exactly 2 (msg1, msg2), ignoring msgOwn
        long unreadForAhmed = messageService.countUnread(chatId, lastExisting.getId(), 1L);
        assertThat(unreadForAhmed).isEqualTo(2);

        // 3. Ahmed marks read up to msg1
        mockMvc.perform(post("/api/chats/{chatId}/read", chatId)
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"messageId\": " + msg1.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Now only msg2 is unread for Ahmed
        long unreadAfterPartialRead = messageService.countUnread(chatId, msg1.getId(), 1L);
        assertThat(unreadAfterPartialRead).isEqualTo(1);
    }
}
