package com.example.chat.user.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class User {

    private Long id;

    private String username;

    private String displayName;

    private String avatarUrl;

    private LocalDateTime createdAt = LocalDateTime.now();
}
