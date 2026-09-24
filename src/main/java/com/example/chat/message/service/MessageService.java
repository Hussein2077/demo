package com.example.chat.message.service;

import com.example.chat.attachment.entity.MessageAttachment;
import com.example.chat.attachment.repository.MessageAttachmentRepository;
import com.example.chat.common.exception.AccessDeniedException;
import com.example.chat.common.exception.ResourceNotFoundException;
import com.example.chat.conversation.repository.ConversationParticipantRepository;
import com.example.chat.conversation.repository.ConversationRepository;
import com.example.chat.conversation.dto.GlobalChatEvent;
import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.message.dto.MessageResponse;
import com.example.chat.message.entity.Message;
import com.example.chat.message.entity.MessageType;
import com.example.chat.message.mapper.MessageMapper;
import com.example.chat.message.repository.MessageRepository;
import com.example.chat.user.entity.User;
import com.example.chat.user.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.example.chat.sse.service.ChatSseService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ConversationParticipantRepository participantRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private final ChatSseService sseService;

    public MessageService(MessageRepository messageRepository,
                          MessageAttachmentRepository attachmentRepository,
                          UserRepository userRepository,
                          ConversationParticipantRepository participantRepository,
                          ConversationRepository conversationRepository,
                          MessageMapper messageMapper,
                          SimpMessagingTemplate messagingTemplate,
                          ChatSseService sseService) {
        this.messageRepository = messageRepository;
        this.attachmentRepository = attachmentRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.conversationRepository = conversationRepository;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
        this.sseService = sseService;

    }

    public Message createMessage(Long conversationId, Long senderId, MessageType type, String content) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(type);
        message.setContent(content);
        Message saved = messageRepository.save(message);
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setUpdatedAt(saved.getCreatedAt());
            conversationRepository.save(conversation);
        });
        return saved;
    }

    public MessageResponse sendTextMessage(Long chatId, Long senderId, String content) {
        if (!participantRepository.existsByIdUserIdAndIdConversationId(senderId, chatId)) {
            throw new AccessDeniedException("You are not a member of this conversation");
        }

        Message message = createMessage(chatId, senderId, MessageType.TEXT, content);
        MessageResponse response = buildResponse(message);
        
        broadcastMessageEvent(chatId, response, message.getCreatedAt());

        return response;
    }

    public void broadcastMessageEvent(Long chatId, MessageResponse response, java.time.LocalDateTime createdAt) {
        // Broadcast to chat destination
        messagingTemplate.convertAndSend("/topic/chats/" + chatId, response);
        
        // Broadcast to each participant's global chat list stream
        List<ConversationParticipant> participants = participantRepository.findByIdConversationId(chatId);
        for (ConversationParticipant cp : participants) {
            long unreadCount = countUnread(chatId, cp.getLastReadMessageId(), cp.getId().getUserId());
            GlobalChatEvent event = new GlobalChatEvent("NEW_MESSAGE", chatId, response, unreadCount, createdAt);
            messagingTemplate.convertAndSend("/topic/users/" + cp.getId().getUserId() + "/chats", event);
            sseService.sendEvent(cp.getId().getUserId(), event);
        }
    }

    public List<MessageResponse> getHistory(Long conversationId, int limit, Long beforeId) {
        List<Message> messages;
        if (beforeId != null) {
            messages = messageRepository.findByConversationIdAndIdBeforeOrderByIdDesc(
                    conversationId, beforeId, PageRequest.of(0, limit));
        } else {
            messages = messageRepository.findLatestByConversationId(
                    conversationId, PageRequest.of(0, limit));
        }

        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> messageIds = messages.stream().map(Message::getId).toList();
        List<Long> senderIds = messages.stream().map(Message::getSenderId).distinct().toList();

        Map<Long, User> usersById = userRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, List<MessageAttachment>> attachmentsByMessageId = attachmentRepository
                .findByMessageIdIn(messageIds).stream()
                .collect(Collectors.groupingBy(MessageAttachment::getMessageId));

        List<MessageResponse> result = messages.stream()
                .map(m -> messageMapper.toResponse(
                        m,
                        usersById.get(m.getSenderId()),
                        attachmentsByMessageId.getOrDefault(m.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());

        // Return in chronological order
        Collections.reverse(result);
        return result;
    }

    public MessageResponse buildResponse(Message message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + message.getSenderId()));
        List<MessageAttachment> attachments = attachmentRepository.findByMessageId(message.getId());
        return messageMapper.toResponse(message, sender, attachments);
    }

    public Optional<MessageResponse> getLastMessage(Long conversationId) {
        return messageRepository.findFirstByConversationIdOrderByIdDesc(conversationId)
                .map(m -> {
                    User sender = userRepository.findById(m.getSenderId()).orElse(null);
                    if (sender == null) return null;
                    List<MessageAttachment> attachments = attachmentRepository.findByMessageId(m.getId());
                    return messageMapper.toResponse(m, sender, attachments);
                });
    }

    public long countUnread(Long conversationId, Long lastReadMessageId, Long userId) {
        return messageRepository.countUnreadMessages(conversationId, lastReadMessageId != null ? lastReadMessageId : 0L, userId);
    }

    public Message findById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
    }
}
