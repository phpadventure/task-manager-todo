package com.taskmanager.websocket;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.taskmanager.dto.websocket.ClientAckMessage;
import com.taskmanager.dto.websocket.ClientSubscribeMessage;
import com.taskmanager.dto.websocket.NotificationMessage;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.security.CustomOAuth2User;
import com.taskmanager.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.Header;
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
    private final Map<UUID, Map<UUID, Set<UUID>>> taskToNotificationIdMap =
            new ConcurrentHashMap<>();

    // AS FOR MVP WE ASSUME ONLY ONE SESSION!!!! BUT USER CAN HAVE A SET OF SESSION
    private final Map<UUID, String> userSessionMap = new ConcurrentHashMap<>();

    public TaskNotificationHandler(TaskService taskService,
            SimpMessagingTemplate messagingTemplate) {
        this.taskService = taskService;
        this.messagingTemplate = messagingTemplate;
    }

    @SubscribeMapping("/notifications")
    public List<NotificationMessage> handleSubscribe(
            Authentication authentication,
            @Header("simpSessionId") String stompSessionId) {
        User userDetails = ((CustomOAuth2User) authentication.getPrincipal()).getUser();
        UUID userId = userDetails.getId();
        String principal = authentication.getName(); // Use principal name for STOMP routing

        String previousSession = userSessionMap.get(userId);
        if (previousSession != null && !previousSession.equals(principal)) {
            LOGGER.warn("User {} reconnected. Old: {}, New: {}, StompId: {}",
                userId, previousSession, principal, stompSessionId);
            removeUser(userId);
        }

        // Initialize/preserve sent notifications (don't overwrite on reconnect)
        sentNotifications.computeIfAbsent(userId, k -> new HashSet<>());
        userIntervals.putIfAbsent(userId, 60000L);
        lastSentTime.putIfAbsent(userId, System.currentTimeMillis());
        userSessionMap.put(userId, principal); // Store principal name for convertAndSendToUser()

        LOGGER.info("User {} subscribed to notifications. Principal: {}, StompSessionId: {}",
            userId, principal, stompSessionId);

        List<NotificationMessage> initialBatch = getOverdueAndDueSoonNotifications(userId);
        // Filter: don't send notifications for tasks that had any notification acked
        Map<UUID, Set<UUID>> userTaskMap = taskToNotificationIdMap.getOrDefault(userId,
                new ConcurrentHashMap<>());
        initialBatch = initialBatch.stream()
                .filter(notification -> {
                    UUID taskId = notification.getTaskId();
                    Set<UUID> prevNotificationIds = userTaskMap.getOrDefault(taskId,
                            ConcurrentHashMap.newKeySet());
                    // Skip if any previous notification for this task was acked
                    boolean anyAcked = prevNotificationIds.stream()
                            .anyMatch(notifId -> hasBeenAcked(userId, notifId));
                    return !anyAcked;
                })
                .collect(Collectors.toList());
        trackSentNotifications(userId, initialBatch);
        updateLastSentTime(userId, System.currentTimeMillis());
        return initialBatch;
    }

    @MessageMapping("/subscribe")
    public void handleSubscribeMessage(ClientSubscribeMessage message,
            Authentication authentication) {
        try {
            User userDetails = ((CustomOAuth2User) authentication.getPrincipal()).getUser();
            UUID userId = userDetails.getId();
            if (message.getInterval() != null && message.getInterval() > 0) {
                userIntervals.put(userId, message.getInterval());
                LOGGER.debug("Updated notification interval for user {}: {} ms",
                    userId, message.getInterval());
            }
        } catch (Exception e) {
            LOGGER.error("Error handling subscribe message", e);
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
        Map<UUID, Set<UUID>> taskMap = taskToNotificationIdMap.computeIfAbsent(userId,
                k -> new ConcurrentHashMap<>());
        for (NotificationMessage notification : notifications) {
            sent.add(notification.getTaskId() + ":" + notification.getReason());
            taskMap.computeIfAbsent(notification.getTaskId(), k -> ConcurrentHashMap.newKeySet())
                    .add(notification.getId());
        }
    }

    private void trackNotificationForTask(UUID userId, UUID taskId, UUID notificationId) {
        taskToNotificationIdMap.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(taskId, k -> ConcurrentHashMap.newKeySet())
                .add(notificationId);
    }

    private boolean hasBeenAcked(UUID userId, UUID notificationId) {
        Set<String> sent = sentNotifications.getOrDefault(userId, new HashSet<>());
        return sent.contains("acked:" + notificationId);
    }

    public boolean hasAlreadySent(UUID userId, UUID taskId, String reason) {
        Set<String> sent = sentNotifications.getOrDefault(userId, new HashSet<>());
        return sent.contains(taskId + ":" + reason);
    }

    public void sendNotificationToUser(UUID userId, NotificationMessage notification) {
        if (!userSessionMap.containsKey(userId)) {
            LOGGER.debug("User {} not connected, skipping notification: taskId={}",
                userId, notification.getTaskId());
            return;
        }

        try {
            String userName = userSessionMap.get(userId);
            LOGGER.info("Sending notification to user {} with sessionId={}: taskId={}, reason={}",
                userId, userName, notification.getTaskId(), notification.getReason());
            messagingTemplate.convertAndSendToUser(
                    userName,
                    NOTIFICATION_QUEUE,
                    notification
            );
            LOGGER.info("Successfully sent notification to user {}: taskId={}, reason={}",
                userId, notification.getTaskId(), notification.getReason());
        } catch (Exception e) {
            LOGGER.error("Error sending notification to user {}: taskId={}, sessionId={}",
                userId, notification.getTaskId(), userSessionMap.get(userId), e);
        }
    }

    public void sendAndTrackNotification(UUID userId,
            NotificationMessage notification) {
        sendNotificationToUser(userId, notification);
        Set<String> sent = sentNotifications.computeIfAbsent(userId,
                k -> new HashSet<>());
        sent.add(notification.getTaskId() + ":" + notification.getReason());
        trackNotificationForTask(userId, notification.getTaskId(), notification.getId());
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
        taskToNotificationIdMap.remove(userId);
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
        try {
            String disconnectSessionId = event.getSessionId();
            if (event.getUser() != null) {
                String principal = event.getUser().getName();
                userSessionMap.forEach((userId, storedPrincipal) -> {
                    if (storedPrincipal != null && storedPrincipal.equals(principal)) {
                        LOGGER.info("User {} disconnected: sessionId={}, principal={}",
                            userId, disconnectSessionId, principal);
                        removeUser(userId);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error handling WebSocket disconnect", e);
        }
    }
}
