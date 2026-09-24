package com.example.chat.message.dto;

/**
 * Payload for marking a message as read via WebSocket.
 */
public class ReadMessageRequest {
    private Long messageId;

    public ReadMessageRequest() {}

    public ReadMessageRequest(Long messageId) {
        this.messageId = messageId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }
}
