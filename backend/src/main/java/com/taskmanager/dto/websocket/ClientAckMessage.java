package com.taskmanager.dto.websocket;

import java.util.UUID;

public class ClientAckMessage {
    private String type;
    private UUID notificationId;

    public ClientAckMessage() {
    }

    public ClientAckMessage(String type, UUID notificationId) {
        this.type = type;
        this.notificationId = notificationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }
}
