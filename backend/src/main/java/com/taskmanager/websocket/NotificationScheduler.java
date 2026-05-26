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
            Long userInterval = null;
            Long currentTime = null;
            Long lastSent = null;

            try {
                userInterval = notificationHandler.getUserInterval(userId);
            } catch (Exception e) {
                LOGGER.error("Error retrieving user interval for user {}", userId, e);
                return;
            }

            try {
                currentTime = System.currentTimeMillis();
                lastSent = notificationHandler.getLastSentTime(userId);
            } catch (Exception e) {
                LOGGER.error("Error retrieving timing information for user {}", userId, e);
                return;
            }

            if (currentTime - lastSent >= userInterval) {
                List<NotificationMessage> notifications = null;
                try {
                    notifications = getNewNotificationsForUser(userId);
                } catch (Exception e) {
                    LOGGER.error("Error fetching notifications for user {}", userId, e);
                    return;
                }

                try {
                    for (NotificationMessage notification : notifications) {
                        try {
                            notificationHandler.sendAndTrackNotification(userId, notification);
                        } catch (Exception e) {
                            LOGGER.error(
                                    "Error sending notification to user {}: notificationId={}",
                                    userId, notification.getId(), e);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Error sending batch notifications for user {}", userId, e);
                }

                try {
                    notificationHandler.updateLastSentTime(userId, currentTime);
                } catch (Exception e) {
                    LOGGER.error("Error updating last sent time for user {}", userId, e);
                }

                LOGGER.debug(
                        "Sent {} notifications for user {}", notifications.size(), userId);
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected error in sendNotificationsForUser for user {}", userId, e);
        }
    }

    private List<NotificationMessage> getNewNotificationsForUser(UUID userId) {
        List<NotificationMessage> notifications = new ArrayList<>();

        try {
            List<Task> overdueTasks = taskService.getOverdueTasks(userId);
            for (Task task : overdueTasks) {
                try {
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
                } catch (Exception e) {
                    LOGGER.error(
                            "Error processing overdue task notification: userId={}, taskId={}",
                            userId, task.getId(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching overdue tasks for user {}", userId, e);
        }

        try {
            List<Task> dueSoonTasks = taskService.getDueSoonTasks(userId);
            for (Task task : dueSoonTasks) {
                try {
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
                } catch (Exception e) {
                    LOGGER.error(
                            "Error processing due-soon task notification: userId={}, taskId={}",
                            userId, task.getId(), e);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error fetching due-soon tasks for user {}", userId, e);
        }

        return notifications;
    }
}
