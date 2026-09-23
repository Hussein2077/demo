package com.example.chat.attachment.mapper;

import com.example.chat.attachment.dto.AttachmentResponse;
import com.example.chat.attachment.entity.MessageAttachment;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(MessageAttachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );
    }
}
