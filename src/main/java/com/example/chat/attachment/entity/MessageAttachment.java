package com.example.chat.attachment.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MessageAttachment {

    private Long id;

    private Long messageId;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private String storageKey;

    private LocalDateTime createdAt = LocalDateTime.now();
}
