package com.example.chat.message.mapper;

import com.example.chat.attachment.dto.AttachmentResponse;
import com.example.chat.attachment.mapper.AttachmentMapper;
import com.example.chat.attachment.entity.MessageAttachment;
import com.example.chat.message.dto.MessageResponse;
import com.example.chat.message.entity.Message;
import com.example.chat.user.mapper.UserMapper;
import com.example.chat.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageMapper {

    private final UserMapper userMapper;
    private final AttachmentMapper attachmentMapper;

    public MessageMapper(UserMapper userMapper, AttachmentMapper attachmentMapper) {
        this.userMapper = userMapper;
        this.attachmentMapper = attachmentMapper;
    }

    public MessageResponse toResponse(Message message, User sender, List<MessageAttachment> attachments) {
        List<AttachmentResponse> attachmentResponses = attachments.stream()
                .map(attachmentMapper::toResponse)
                .toList();

        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                userMapper.toResponse(sender),
                message.getType(),
                message.getContent(),
                attachmentResponses,
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
