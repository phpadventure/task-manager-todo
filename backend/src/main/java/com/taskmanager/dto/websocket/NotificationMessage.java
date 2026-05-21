package com.taskmanager.dto.websocket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.taskmanager.entity.TaskPriority;

public class NotificationMessage {
    private String type;
    private UUID id;
    private UUID taskId;
    private String title;
    private String reason;
    private LocalDate dueDate;
    private TaskPriority priority;
    private LocalDateTime sentAt;

    public NotificationMessage() {
    }

    public NotificationMessage(UUID id, UUID taskId, String title, String reason,
                               LocalDate dueDate, TaskPriority priority) {
        this.type = "notification";
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.reason = reason;
        this.dueDate = dueDate;
        this.priority = priority;
        this.sentAt = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
