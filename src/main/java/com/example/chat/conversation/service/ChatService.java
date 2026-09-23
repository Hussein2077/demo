package com.example.chat.conversation.service;

import com.example.chat.common.exception.AccessDeniedException;
import com.example.chat.common.exception.ResourceNotFoundException;
import com.example.chat.conversation.dto.ChatSummaryResponse;
import com.example.chat.conversation.dto.ConversationResponse;
import com.example.chat.conversation.entity.Conversation;
import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.conversation.entity.ConversationParticipantId;
import com.example.chat.conversation.entity.ConversationType;
import com.example.chat.conversation.mapper.ConversationMapper;
import com.example.chat.conversation.repository.ConversationParticipantRepository;
import com.example.chat.conversation.repository.ConversationRepository;
import com.example.chat.message.dto.MessageResponse;
import com.example.chat.message.entity.Message;
import com.example.chat.message.service.MessageService;
import com.example.chat.user.entity.User;
import com.example.chat.user.repository.UserRepository;
import com.example.chat.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageService messageService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;

    public ChatService(ConversationRepository conversationRepository,
                       ConversationParticipantRepository participantRepository,
                       MessageService messageService,
                       UserService userService,
                       UserRepository userRepository,
                       ConversationMapper conversationMapper) {
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageService = messageService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.conversationMapper = conversationMapper;
    }

    @Transactional(readOnly = true)
    public void assertParticipant(Long userId, Long conversationId) {
        if (!participantRepository.existsByIdUserIdAndIdConversationId(userId, conversationId)) {
            throw new AccessDeniedException("You are not a member of this conversation");
        }
    }

    @Transactional(readOnly = true)
    public Conversation findConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));
    }

    @Transactional(readOnly = true)
    public List<ChatSummaryResponse> listChats(Long userId) {
        List<ConversationParticipant> memberships = participantRepository.findByIdUserId(userId);

        return memberships.stream()
                .map(membership -> {
                    Conversation conversation = membership.getConversation();
                    Optional<MessageResponse> lastMessage = messageService.getLastMessage(conversation.getId());
                    long unreadCount = messageService.countUnread(
                            conversation.getId(),
                            membership.getLastReadMessageId(),
                            userId
                    );

                    String displayName = conversation.getName();
                    String avatarUrl = conversation.getAvatarUrl();

                    if (conversation.getType() == ConversationType.PRIVATE) {
                        List<ConversationParticipant> participants = participantRepository.findByIdConversationId(conversation.getId());
                        Optional<Long> otherUserId = participants.stream()
                                .map(p -> p.getId().getUserId())
                                .filter(id -> !id.equals(userId))
                                .findFirst();
                        if (otherUserId.isPresent()) {
                            Optional<User> otherUser = userRepository.findById(otherUserId.get());
                            if (otherUser.isPresent()) {
                                displayName = otherUser.get().getDisplayName();
                                avatarUrl = otherUser.get().getAvatarUrl();
                            }
                        }
                    }

                    return conversationMapper.toSummary(conversation, displayName, avatarUrl, lastMessage.orElse(null), unreadCount);
                })
                .sorted((a, b) -> b.getUpdatedAt().compareTo(a.getUpdatedAt()))
                .toList();
    }

    @Transactional
    public ConversationResponse getOrCreatePrivateChat(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot create a chat with yourself");
        }

        userService.findById(targetUserId); // validates target exists

        List<Long> existingIds = participantRepository.findPrivateConversationBetween(currentUserId, targetUserId);
        if (!existingIds.isEmpty()) {
            Conversation existing = conversationRepository.findById(existingIds.get(0))
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            return conversationMapper.toResponse(existing);
        }

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.PRIVATE);
        conversationRepository.save(conversation);

        participantRepository.save(new ConversationParticipant(conversation, currentUserId));
        participantRepository.save(new ConversationParticipant(conversation, targetUserId));

        return conversationMapper.toResponse(conversation);
    }

    @Transactional
    public void markRead(Long conversationId, Long userId, Long messageId) {
        Message message = messageService.findById(messageId);
        if (!message.getConversationId().equals(conversationId)) {
            throw new IllegalArgumentException("Message does not belong to this conversation");
        }

        ConversationParticipantId participantId = new ConversationParticipantId(conversationId, userId);
        ConversationParticipant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this conversation"));
        participant.setLastReadMessageId(messageId);
        participantRepository.save(participant);
    }
}
