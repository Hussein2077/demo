package com.example.chat.conversation.repository;

import com.example.chat.conversation.entity.Conversation;

import java.util.Optional;

public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(Long id);
    Optional<Conversation> findByName(String name);
}
