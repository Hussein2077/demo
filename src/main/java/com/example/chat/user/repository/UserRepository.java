package com.example.chat.user.repository;

import com.example.chat.user.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    User save(User user);
    List<User> findAllById(Iterable<Long> ids);
    Optional<User> findByUsernameIgnoreCase(String username);
    List<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String username, String displayName);
}
