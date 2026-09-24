package com.example.chat.user.repository;

import com.example.chat.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        storage.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> findAllById(Iterable<Long> ids) {
        List<User> users = new ArrayList<>();
        for (Long id : ids) {
            User user = storage.get(id);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public Optional<User> findByUsernameIgnoreCase(String username) {
        return storage.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    @Override
    public List<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String username, String displayName) {
        return storage.values().stream()
                .filter(u -> u.getUsername().toLowerCase().contains(username.toLowerCase()) ||
                             u.getDisplayName().toLowerCase().contains(displayName.toLowerCase()))
                .collect(Collectors.toList());
    }
}
