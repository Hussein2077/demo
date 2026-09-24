package com.example.chat.attachment.controller;

import com.example.chat.attachment.service.AttachmentService;
import com.example.chat.common.response.ApiResponse;
import com.example.chat.config.CurrentUserProvider;
import com.example.chat.message.dto.MessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Files")
public class FileController {

    private final AttachmentService attachmentService;
    private final CurrentUserProvider currentUserProvider;

    public FileController(AttachmentService attachmentService, CurrentUserProvider currentUserProvider) {
        this.attachmentService = attachmentService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping(value = "/api/chats/{chatId}/messages/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file message to a chat")
    public ResponseEntity<ApiResponse<MessageResponse>> uploadFileMessage(
            @PathVariable Long chatId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "content", required = false) String content) {

        Long currentUserId = currentUserProvider.getCurrentUserId();
        MessageResponse response = attachmentService.uploadFileMessage(chatId, currentUserId, file, content);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/files/{attachmentId}")
    @Operation(summary = "Download a file attachment")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long attachmentId,
            @RequestParam(value = "userId", required = false) Long userIdParam) {
        Long currentUserId;
        try {
            currentUserId = currentUserProvider.getCurrentUserId();
        } catch (Exception ex) {
            if (userIdParam == null) {
                throw new IllegalArgumentException("Missing required header: X-User-Id");
            }
            currentUserId = userIdParam;
        }
        AttachmentService.AttachmentDownload download = attachmentService.getFileForUser(attachmentId, currentUserId);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(download.attachment().getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        // Choose inline for viewable types (images, PDF, plain text) otherwise force download
        boolean inline = mediaType.isCompatibleWith(MediaType.IMAGE_JPEG) ||
                mediaType.isCompatibleWith(MediaType.IMAGE_PNG) ||
                mediaType.isCompatibleWith(MediaType.IMAGE_GIF) ||
                mediaType.equals(MediaType.APPLICATION_PDF) ||
                mediaType.isCompatibleWith(MediaType.TEXT_PLAIN) ||
                mediaType.isCompatibleWith(MediaType.TEXT_HTML);
        String disposition = inline ? "inline" : "attachment";
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition + "; filename=\"" + download.attachment().getFileName() + "\"")
                .body(download.resource());
    }
}
