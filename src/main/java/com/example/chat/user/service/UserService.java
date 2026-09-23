package com.example.chat.user.service;

import com.example.chat.common.exception.ResourceNotFoundException;
import com.example.chat.user.dto.UserResponse;
import com.example.chat.user.entity.User;
import com.example.chat.user.mapper.UserMapper;
import com.example.chat.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String query, Long currentUserId) {
        return userRepository
                .findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(query, query)
                .stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .map(userMapper::toResponse)
                .toList();
    }
}
