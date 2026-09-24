package com.example.chat.sse.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service that manages Server‑Sent Events (SSE) emitters per user.
 *
 * <p>It keeps a thread‑safe map of {@code userId -> Set<SseEmitter>} so that multiple
 * connections (e.g., from different tabs) can be supported. Emitters are removed
 * automatically on completion, timeout or error. A heartbeat comment is sent
 * every 25 seconds to keep the connection alive.
 */
@Service
public class ChatSseService {

    /** Timeout for an emitter – 30 minutes of inactivity. */
    private static final long EMITTER_TIMEOUT_MS = Duration.ofMinutes(30).toMillis();

    /** Heartbeat interval – 25 seconds. */
    private static final long HEARTBEAT_INTERVAL_MS = Duration.ofSeconds(25).toMillis();

    /** Map of user id to active emitters. */
    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    private final ScheduledExecutorService heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();

    public ChatSseService() {
        // Start periodic heartbeat task
        heartbeatScheduler.scheduleAtFixedRate(this::sendHeartbeats, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Register a new SSE emitter for the given user.
     *
     * @param userId the {@code X-User-Id} of the client
     * @return a configured {@link SseEmitter}
     */
    public SseEmitter register(Long userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);
        // Store emitter
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(emitter);
        // Cleanup callbacks
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));
        return emitter;
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        Set<SseEmitter> set = userEmitters.get(userId);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    /**
     * Send an event payload to a single user.
     */
    public void sendEvent(Long userId, Object payload) {
        Set<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArraySet<>());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("chat-event").data(payload));
            } catch (IOException e) {
                // If sending fails, clean up the emitter
                removeEmitter(userId, emitter);
            }
        }
    }

    /**
     * Broadcast an event to multiple users.
     */
    public void broadcastEvent(Set<Long> userIds, Object payload) {
        for (Long userId : userIds) {
            sendEvent(userId, payload);
        }
    }

    /**
     * Periodic heartbeat – sends a comment line to all active emitters.
     */
    private void sendHeartbeats() {
        for (Set<SseEmitter> emitters : userEmitters.values()) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException ignored) {
                    // Failure will be cleaned up by the emitter's error callback
                }
            }
        }
    }
}
