package com.example.chat.conversation.repository;

import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.conversation.entity.ConversationParticipantId;
import com.example.chat.conversation.entity.ConversationType;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class InMemoryConversationParticipantRepository implements ConversationParticipantRepository {

    private final Map<ConversationParticipantId, ConversationParticipant> storage = new ConcurrentHashMap<>();

    @Override
    public ConversationParticipant save(ConversationParticipant participant) {
        storage.put(participant.getId(), participant);
        return participant;
    }

    @Override
    public Optional<ConversationParticipant> findById(ConversationParticipantId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<ConversationParticipant> findByIdUserId(Long userId) {
        return storage.values().stream()
                .filter(cp -> cp.getId().getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ConversationParticipant> findByIdConversationId(Long conversationId) {
        return storage.values().stream()
                .filter(cp -> cp.getId().getConversationId().equals(conversationId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByIdUserIdAndIdConversationId(Long userId, Long conversationId) {
        return storage.containsKey(new ConversationParticipantId(conversationId, userId));
    }

    @Override
    public List<Long> findPrivateConversationBetween(Long user1, Long user2) {
        Map<Long, List<ConversationParticipant>> byConv = storage.values().stream()
                .collect(Collectors.groupingBy(cp -> cp.getId().getConversationId()));

        List<Long> result = new ArrayList<>();
        for (Map.Entry<Long, List<ConversationParticipant>> entry : byConv.entrySet()) {
            List<ConversationParticipant> participants = entry.getValue();
            if (participants.isEmpty()) continue;
            
            if (participants.get(0).getConversation() != null && 
                participants.get(0).getConversation().getType() == ConversationType.PRIVATE) {
                
                boolean hasUser1 = participants.stream().anyMatch(cp -> cp.getId().getUserId().equals(user1));
                boolean hasUser2 = participants.stream().anyMatch(cp -> cp.getId().getUserId().equals(user2));
                
                if (hasUser1 && hasUser2) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;
    }

    @Override
    public List<Long> findConversationsWithExactParticipants(List<Long> userIds, long size) {
        Map<Long, List<ConversationParticipant>> byConv = storage.values().stream()
                .collect(Collectors.groupingBy(cp -> cp.getId().getConversationId()));

        List<Long> result = new ArrayList<>();
        for (Map.Entry<Long, List<ConversationParticipant>> entry : byConv.entrySet()) {
            List<ConversationParticipant> participants = entry.getValue();
            if (participants.size() == size) {
                boolean hasAll = true;
                for (Long uid : userIds) {
                    if (participants.stream().noneMatch(cp -> cp.getId().getUserId().equals(uid))) {
                        hasAll = false;
                        break;
                    }
                }
                if (hasAll) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;
    }
}
