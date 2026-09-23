package com.example.chat.attachment.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredFile {
    private final String storageKey;
    private final String originalFileName;
    private final String contentType;
    private final long fileSize;
}
