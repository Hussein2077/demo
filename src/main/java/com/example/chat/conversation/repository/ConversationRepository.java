package com.example.chat.conversation.repository;

import com.example.chat.conversation.entity.Conversation;
public interface ConversationRepository {
    Conversation save(Conversation conversation);
    Optional<Conversation> findById(Long id);
    Optional<Conversation> findByName(String name);
}
