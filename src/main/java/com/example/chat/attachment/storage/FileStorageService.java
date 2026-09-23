package com.example.chat.attachment.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile store(MultipartFile file);
    Resource load(String storageKey);
    void delete(String storageKey);
}
