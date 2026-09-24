package com.example.chat.read;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Service that tracks the last read message per user per conversation.
 * In‑memory implementation suitable for the demo (no persistence).
 */
@Service
public class ReadStateService {

    // userId -> (chatId -> lastReadMessageId)
    private final ConcurrentMap<Long, ConcurrentMap<Long, Long>> readMap = new ConcurrentHashMap<>();

    public void markRead(Long userId, Long chatId, Long messageId) {
        readMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .merge(chatId, messageId, Math::max); // keep the highest messageId
    }

    public Long getLastRead(Long userId, Long chatId) {
        return readMap.getOrDefault(userId, new ConcurrentHashMap<>()).get(chatId);
    }
}
