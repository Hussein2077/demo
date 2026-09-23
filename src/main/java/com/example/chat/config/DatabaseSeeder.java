package com.example.chat.config;

import com.example.chat.conversation.entity.Conversation;
import com.example.chat.conversation.entity.ConversationParticipant;
import com.example.chat.conversation.entity.ConversationType;
import com.example.chat.conversation.repository.ConversationParticipantRepository;
import com.example.chat.conversation.repository.ConversationRepository;
import com.example.chat.message.entity.Message;
import com.example.chat.message.entity.MessageType;
import com.example.chat.message.repository.MessageRepository;
import com.example.chat.user.entity.User;
import com.example.chat.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;

    public DatabaseSeeder(UserRepository userRepository,
                          ConversationRepository conversationRepository,
                          ConversationParticipantRepository participantRepository,
                          MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Static 5 Users
        User ahmed = getOrCreateUser("ahmed", "Ahmed Mohamed");
        User mohamed = getOrCreateUser("mohamed", "Mohamed Ali");
        User ali = getOrCreateUser("ali", "Ali Hassan");
        User sara = getOrCreateUser("sara", "Sara Ahmed");
        User omar = getOrCreateUser("omar", "Omar Yasser");

        List<User> allUsers = List.of(ahmed, mohamed, ali, sara, omar);

        // 2. Static Group: "System Chat"
        Conversation systemChat = getOrCreateGroup("System Chat", allUsers);
        seedMessagesIfFew(systemChat.getId(), allUsers, 110, "System Chat message #");

        // 3. Sample Private Conversations
        Conversation ahmedMohamed = getOrCreatePrivateChat(ahmed, mohamed);
        seedMessagesIfFew(ahmedMohamed.getId(), List.of(ahmed, mohamed), 55, "Message between Ahmed and Mohamed #");

        Conversation ahmedAli = getOrCreatePrivateChat(ahmed, ali);
        seedMessagesIfFew(ahmedAli.getId(), List.of(ahmed, ali), 35, "Message between Ahmed and Ali #");

        Conversation ahmedSara = getOrCreatePrivateChat(ahmed, sara);
        seedMessagesIfFew(ahmedSara.getId(), List.of(ahmed, sara), 15, "Message between Ahmed and Sara #");
    }

    private User getOrCreateUser(String username, String displayName) {
        Optional<User> existing = userRepository.findByUsernameIgnoreCase(username);
        if (existing.isPresent()) {
            return existing.get();
        }
        User user = new User();
        user.setUsername(username);
        user.setDisplayName(displayName);
        return userRepository.save(user);
    }

    private Conversation getOrCreateGroup(String name, List<User> members) {
        Optional<Conversation> existing = conversationRepository.findByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation group = new Conversation();
        group.setType(ConversationType.GROUP);
        group.setName(name);
        group = conversationRepository.save(group);

        for (User member : members) {
            participantRepository.save(new ConversationParticipant(group, member.getId()));
        }

        return group;
    }

    private Conversation getOrCreatePrivateChat(User user1, User user2) {
        List<Long> existingIds = participantRepository.findPrivateConversationBetween(user1.getId(), user2.getId());
        if (!existingIds.isEmpty()) {
            return conversationRepository.findById(existingIds.get(0)).orElseThrow();
        }

        Conversation conversation = new Conversation();
        conversation.setType(ConversationType.PRIVATE);
        conversation = conversationRepository.save(conversation);

        participantRepository.save(new ConversationParticipant(conversation, user1.getId()));
        participantRepository.save(new ConversationParticipant(conversation, user2.getId()));

        return conversation;
    }

    private void seedMessagesIfFew(Long conversationId, List<User> senders, int targetCount, String prefix) {
        long currentCount = messageRepository.countByConversationId(conversationId);
        if (currentCount >= targetCount) {
            return;
        }

        int toGenerate = (int) (targetCount - currentCount);
        LocalDateTime startTime = LocalDateTime.now().minusDays(3);

        List<Message> messagesToSave = new ArrayList<>(toGenerate);
        for (int i = 1; i <= toGenerate; i++) {
            User sender = senders.get((int) ((currentCount + i) % senders.size()));
            Message message = new Message();
            message.setConversationId(conversationId);
            message.setSenderId(sender.getId());
            message.setType(MessageType.TEXT);
            message.setContent(prefix + (currentCount + i));
            message.setCreatedAt(startTime.plusMinutes((currentCount + i) * 10L));
            message.setUpdatedAt(message.getCreatedAt());
            messagesToSave.add(message);
        }

        messageRepository.saveAll(messagesToSave);
    }
}
