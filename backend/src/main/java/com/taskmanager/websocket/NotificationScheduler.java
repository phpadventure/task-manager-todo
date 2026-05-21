package com.taskmanager.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.taskmanager.dto.websocket.NotificationMessage;
import com.taskmanager.entity.Task;
import com.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class NotificationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            NotificationScheduler.class);

    private final TaskService taskService;
    private final TaskNotificationHandler notificationHandler;

    public NotificationScheduler(TaskService taskService,
            TaskNotificationHandler notificationHandler) {
        this.taskService = taskService;
        this.notificationHandler = notificationHandler;
    }

    @Scheduled(fixedRateString = "${app.notification.interval:10000}")
    public void sendPeriodicNotifications() {
        try {
            LOGGER.debug("Starting periodic notification check");

            for (UUID userId : notificationHandler.getConnectedUsers()) {
                sendNotificationsForUser(userId);
            }

            LOGGER.debug("Periodic notification check completed");
        } catch (Exception e) {
            LOGGER.error("Error in periodic notification scheduler", e);
        }
    }

    public void sendNotificationsForUser(UUID userId) {
        try {
            Long userInterval = notificationHandler.getUserInterval(userId);
            Long currentTime = System.currentTimeMillis();
            Long lastSent = notificationHandler.getLastSentTime(userId);

            if (currentTime - lastSent >= userInterval) {
                List<NotificationMessage> notifications =
                        getNewNotificationsForUser(userId);
                for (NotificationMessage notification : notifications) {
                    notificationHandler.sendAndTrackNotification(userId, notification);
                }
                notificationHandler.updateLastSentTime(userId, currentTime);
                LOGGER.debug(
                        "Sent {} notifications for user {}", notifications.size(), userId);
            }
        } catch (Exception e) {
            LOGGER.error("Error sending notifications for user {}", userId, e);
        }
    }

    private List<NotificationMessage> getNewNotificationsForUser(UUID userId) {
        List<NotificationMessage> notifications = new ArrayList<>();

        List<Task> overdueTasks = taskService.getOverdueTasks(userId);
        for (Task task : overdueTasks) {
            if (!notificationHandler.hasAlreadySent(userId, task.getId(),
                    "overdue")) {
                NotificationMessage notification = new NotificationMessage(
                        UUID.randomUUID(),
                        task.getId(),
                        task.getTitle(),
                        "overdue",
                        task.getDueDate(),
                        task.getPriority()
                );
                notifications.add(notification);
                LOGGER.debug(
                        "New overdue notification: userId={}, taskId={}, title={}",
                        userId, task.getId(), task.getTitle());
            }
        }

        List<Task> dueSoonTasks = taskService.getDueSoonTasks(userId);
        for (Task task : dueSoonTasks) {
            if (!notificationHandler.hasAlreadySent(userId, task.getId(),
                    "due-soon")) {
                NotificationMessage notification = new NotificationMessage(
                        UUID.randomUUID(),
                        task.getId(),
                        task.getTitle(),
                        "due-soon",
                        task.getDueDate(),
                        task.getPriority()
                );
                notifications.add(notification);
                LOGGER.debug(
                        "New due-soon notification: userId={}, taskId={}, title={}",
                        userId, task.getId(), task.getTitle());
            }
        }

        return notifications;
    }
}
