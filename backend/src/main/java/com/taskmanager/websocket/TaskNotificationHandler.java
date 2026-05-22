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
import com.taskmanager.entity.User;
import com.taskmanager.security.CustomOAuth2User;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
public class TaskNotificationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskNotificationHandler.class);
    private static final String NOTIFICATION_QUEUE = "/queue/notifications";

    private final TaskService taskService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<UUID, Set<String>> sentNotifications =
            new ConcurrentHashMap<>();
    private final Map<UUID, Long> userIntervals = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSentTime = new ConcurrentHashMap<>();

    // AS FOR MVP WE ASSUME ONLY ONE SESSION!!!! BUT USER CAN HAVE A SET OF SESSION
    private final Map<UUID, String> userSessionMap = new ConcurrentHashMap<>(); 

    public TaskNotificationHandler(TaskService taskService,
            SimpMessagingTemplate messagingTemplate) {
        this.taskService = taskService;
        this.messagingTemplate = messagingTemplate;
    }

    @SubscribeMapping("/notifications")
    public List<NotificationMessage> handleSubscribe(
            Authentication authentication) {
        User userDetails = ((CustomOAuth2User) authentication.getPrincipal()).getUser();
        UUID userId = userDetails.getId();
        sentNotifications.put(userId, new HashSet<>());
        userIntervals.put(userId, 60000L);

        String username = authentication.getName();
        userSessionMap.put(userId, username);

        List<NotificationMessage> initialBatch = getOverdueAndDueSoonNotifications(userId);
        trackSentNotifications(userId, initialBatch);
        updateLastSentTime(userId, System.currentTimeMillis());
        return initialBatch;
    }

    @MessageMapping("/subscribe")
    public void handleSubscribeMessage(ClientSubscribeMessage message,
            Authentication authentication) {
        User userDetails = ((CustomOAuth2User) authentication.getPrincipal()).getUser();
        UUID userId = userDetails.getId();
        if (message.getInterval() != null && message.getInterval() > 0) {
            userIntervals.put(userId, message.getInterval());
        }
    }

    @MessageMapping("/ack")
    public void handleAckMessage(ClientAckMessage message,
            Authentication authentication) {
        User userDetails = ((CustomOAuth2User) authentication.getPrincipal()).getUser();
        UUID userId = userDetails.getId();
        if (message.getNotificationId() != null) {
            LOGGER.info("User {} acknowledged notification with id {}", userId,
                    message.getNotificationId());
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
        if (!userSessionMap.containsKey(userId)) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                userSessionMap.get(userId),
                NOTIFICATION_QUEUE,
                notification
        );
    }

    public void sendAndTrackNotification(UUID userId,
            NotificationMessage notification) {
        sendNotificationToUser(userId, notification);
        Set<String> sent = sentNotifications.computeIfAbsent(userId,
                k -> new HashSet<>());
        sent.add(notification.getTaskId() + ":" + notification.getReason());
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
        lastSentTime.remove(userId);
        userSessionMap.remove(userId);
    }

    public Set<UUID> getConnectedUsers() {
        return new HashSet<>(userIntervals.keySet());
    }

    public Long getLastSentTime(UUID userId) {
        return lastSentTime.getOrDefault(userId, 0L);
    }

    public void updateLastSentTime(UUID userId, Long timestamp) {
        lastSentTime.put(userId, timestamp);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        if (event.getUser() != null && event.getUser() instanceof SecurityUser) {
            UUID userId = ((SecurityUser) event.getUser()).getId();
            removeUser(userId);
        }
    }
}
