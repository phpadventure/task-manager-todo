package com.taskmanager.websocket;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.taskmanager.dto.websocket.ClientAckMessage;
import com.taskmanager.dto.websocket.ClientSubscribeMessage;
import com.taskmanager.dto.websocket.NotificationMessage;
import com.taskmanager.entity.Task;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.service.TaskService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

@Controller
public class TaskNotificationHandler {

    private final TaskService taskService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<UUID, Set<String>> sentNotifications =
            new ConcurrentHashMap<>();
    private final Map<UUID, Long> userIntervals = new ConcurrentHashMap<>();

    public TaskNotificationHandler(TaskService taskService,
            SimpMessagingTemplate messagingTemplate) {
        this.taskService = taskService;
        this.messagingTemplate = messagingTemplate;
    }

    @SubscribeMapping("/notifications")
    public List<NotificationMessage> handleSubscribe(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((SecurityUser) userDetails).getId();
        sentNotifications.put(userId, new HashSet<>());
        userIntervals.put(userId, 60000L);

        List<NotificationMessage> initialBatch = getOverdueAndDueSoonNotifications(userId);
        trackSentNotifications(userId, initialBatch);
        return initialBatch;
    }

    @MessageMapping("/subscribe")
    public void handleSubscribeMessage(ClientSubscribeMessage message,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((SecurityUser) userDetails).getId();
        if (message.getInterval() != null && message.getInterval() > 0) {
            userIntervals.put(userId, message.getInterval());
        }
    }

    @MessageMapping("/ack")
    public void handleAckMessage(ClientAckMessage message,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = ((SecurityUser) userDetails).getId();
        if (message.getNotificationId() != null) {
            Set<String> acked = sentNotifications.computeIfAbsent(userId, k -> new HashSet<>());
            acked.add("acked:" + message.getNotificationId());
        }
    }

    private List<NotificationMessage> getOverdueAndDueSoonNotifications(UUID userId) {
        List<Task> overdueTasks = taskService.getOverdueTasks(userId);
        List<Task> dueSoonTasks = taskService.getDueSoonTasks(userId);

        List<NotificationMessage> notifications = new java.util.ArrayList<>();

        for (Task task : overdueTasks) {
            notifications.add(new NotificationMessage(
                    UUID.randomUUID(),
                    task.getId(),
                    task.getTitle(),
                    "overdue",
                    task.getDueDate(),
                    task.getPriority()
            ));
        }

        for (Task task : dueSoonTasks) {
            notifications.add(new NotificationMessage(
                    UUID.randomUUID(),
                    task.getId(),
                    task.getTitle(),
                    "due-soon",
                    task.getDueDate(),
                    task.getPriority()
            ));
        }

        return notifications;
    }

    private void trackSentNotifications(UUID userId, List<NotificationMessage> notifications) {
        Set<String> sent = sentNotifications.computeIfAbsent(userId, k -> new HashSet<>());
        for (NotificationMessage notification : notifications) {
            sent.add(notification.getTaskId() + ":" + notification.getReason());
        }
    }

    public boolean hasAlreadySent(UUID userId, UUID taskId, String reason) {
        Set<String> sent = sentNotifications.getOrDefault(userId, new HashSet<>());
        return sent.contains(taskId + ":" + reason);
    }

    public void sendNotificationToUser(UUID userId, NotificationMessage notification) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );
    }

    public void clearSentNotifications(UUID userId) {
        sentNotifications.put(userId, new HashSet<>());
    }

    public Long getUserInterval(UUID userId) {
        return userIntervals.getOrDefault(userId, 60000L);
    }

    public void removeUser(UUID userId) {
        sentNotifications.remove(userId);
        userIntervals.remove(userId);
    }
}
