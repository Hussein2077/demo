package com.example.chat.attachment.repository;

import com.example.chat.attachment.entity.MessageAttachment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryMessageAttachmentRepository implements MessageAttachmentRepository {

    private final Map<Long, MessageAttachment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public MessageAttachment save(MessageAttachment attachment) {
        if (attachment.getId() == null) {
            attachment.setId(idGenerator.getAndIncrement());
        }
        storage.put(attachment.getId(), attachment);
        return attachment;
    }

    @Override
    public Optional<MessageAttachment> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<MessageAttachment> findByMessageIdIn(List<Long> messageIds) {
        return storage.values().stream()
                .filter(a -> messageIds.contains(a.getMessageId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageAttachment> findByMessageId(Long messageId) {
        return storage.values().stream()
                .filter(a -> a.getMessageId().equals(messageId))
                .collect(Collectors.toList());
    }
}
