package com.taskmanager.websocket;

import java.util.UUID;

import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class NotificationScheduler {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskNotificationHandler notificationHandler;

    @Value("${app.notification.interval:60000}")
    private long notificationInterval;

    public NotificationScheduler(TaskService taskService, UserService userService,
            TaskNotificationHandler notificationHandler) {
        this.taskService = taskService;
        this.userService = userService;
        this.notificationHandler = notificationHandler;
    }

    @Scheduled(fixedRateString = "${app.notification.interval:60000}")
    public void sendPeriodicNotifications() {
        // In a production system, you would iterate through connected users
        // For MVP, the handler maintains active user state via subscriptions
        // This scheduler is here for extensibility - can be enhanced to:
        // 1. Query database for all users
        // 2. Send notifications to actively connected clients only
        // 3. Store undelivered notifications for offline users
    }
}
