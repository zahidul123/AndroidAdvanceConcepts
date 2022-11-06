package com.app.plantdisease.models;

public class MessageChatting {
    String messageId, messageBody, messageTime, senderId, receiverId, messageType;

    public MessageChatting(String messageId, String messageBody, String messageTime, String senderId, String receiverId, String messageType) {
        this.messageId = messageId;
        this.messageBody = messageBody;
        this.messageTime = messageTime;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageType = messageType;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
