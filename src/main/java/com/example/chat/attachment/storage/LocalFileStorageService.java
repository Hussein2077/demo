package com.example.chat.attachment.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path storageRoot;

    public LocalFileStorageService(@Value("${chat.files.storage-path}") String storagePath) {
        this.storageRoot = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directory: " + storagePath, e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        String storageKey = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        try {
            Path target = storageRoot.resolve(storageKey);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + originalName, e);
        }

        return new StoredFile(storageKey, originalName, file.getContentType(), file.getSize());
    }

    @Override
    public Resource load(String storageKey) {
        try {
            Path filePath = storageRoot.resolve(storageKey).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("File not found or not readable: " + storageKey);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid storage key: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path filePath = storageRoot.resolve(storageKey).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete file: " + storageKey, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
