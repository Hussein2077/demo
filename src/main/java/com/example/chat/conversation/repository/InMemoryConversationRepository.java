package com.example.chat.conversation.repository;

import com.example.chat.conversation.entity.Conversation;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryConversationRepository implements ConversationRepository {

    private final Map<Long, Conversation> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Conversation save(Conversation conversation) {
        if (conversation.getId() == null) {
            conversation.setId(idGenerator.getAndIncrement());
        }
        storage.put(conversation.getId(), conversation);
        return conversation;
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Conversation> findByName(String name) {
        return storage.values().stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst();
    }
}
