package com.example.chat.message.repository;

import com.example.chat.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryMessageRepository implements MessageRepository {

    private final Map<Long, Message> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Message save(Message message) {
        if (message.getId() == null) {
            message.setId(idGenerator.getAndIncrement());
        }
        storage.put(message.getId(), message);
        return message;
    }

    @Override
    public Optional<Message> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Message> findLatestByConversationId(Long conversationId, Pageable pageable) {
        return storage.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId))
                .sorted(Comparator.comparing(Message::getId).reversed())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationIdAndIdBeforeOrderByIdDesc(Long conversationId, Long beforeId, Pageable pageable) {
        return storage.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId) && m.getId() < beforeId)
                .sorted(Comparator.comparing(Message::getId).reversed())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Message> findFirstByConversationIdOrderByIdDesc(Long conversationId) {
        return storage.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId))
                .max(Comparator.comparing(Message::getId));
    }

    @Override
    public long countByConversationId(Long conversationId) {
        return storage.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId))
                .count();
    }

    @Override
    public long countUnreadMessages(Long conversationId, Long lastReadMessageId, Long userId) {
        return storage.values().stream()
                .filter(m -> m.getConversationId().equals(conversationId) 
                        && m.getId() > lastReadMessageId 
                        && !m.getSenderId().equals(userId))
                .count();
    }
}
