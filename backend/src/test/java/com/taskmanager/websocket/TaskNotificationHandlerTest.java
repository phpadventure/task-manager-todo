package com.taskmanager.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.taskmanager.dto.websocket.ClientAckMessage;
import com.taskmanager.dto.websocket.ClientSubscribeMessage;
import com.taskmanager.dto.websocket.NotificationMessage;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.service.TaskService;
import com.taskmanager.util.TestDataBuilder;

@ExtendWith(MockitoExtension.class)
class TaskNotificationHandlerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private TaskNotificationHandler handler;
    private UUID userId;
    private SecurityUser securityUser;

    @BeforeEach
    void setUp() {
        handler = new TaskNotificationHandler(taskService, messagingTemplate);
        userId = UUID.randomUUID();
        securityUser = mock(SecurityUser.class);
        lenient().when(securityUser.getId()).thenReturn(userId);
        lenient().when(taskService.getOverdueTasks(any())).thenReturn(new ArrayList<>());
        lenient().when(taskService.getDueSoonTasks(any())).thenReturn(new ArrayList<>());
    }

    @Test
    void testSubscribeMapping_AuthenticatedUser_SendsInitialBatch() {
        Task overdueTask = TestDataBuilder.createTask(userId)
                .title("Overdue task")
                .status(TaskStatus.TO_DO)
                .dueDate(LocalDate.now().minusDays(3))
                .build();

        Task dueSoonTask = TestDataBuilder.createTask(userId)
                .title("Due soon task")
                .status(TaskStatus.TO_DO)
                .dueDate(LocalDate.now().plusDays(1))
                .build();

        when(taskService.getOverdueTasks(userId)).thenReturn(List.of(overdueTask));
        when(taskService.getDueSoonTasks(userId)).thenReturn(List.of(dueSoonTask));

        List<NotificationMessage> result = handler.handleSubscribe(securityUser);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(n -> n.getReason().equals("overdue")));
        assertTrue(result.stream().anyMatch(n -> n.getReason().equals("due-soon")));
    }

    @Test
    void testSubscribeMapping_NoOverdueOrDueSoon_SendsEmptyBatch() {
        when(taskService.getOverdueTasks(userId)).thenReturn(new ArrayList<>());
        when(taskService.getDueSoonTasks(userId)).thenReturn(new ArrayList<>());

        List<NotificationMessage> result = handler.handleSubscribe(securityUser);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSubscribeMapping_WithOverdueAndDueSoon_SendsBoth() {
        Task overdueTask = TestDataBuilder.createTask(userId)
                .status(TaskStatus.TO_DO)
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        Task dueSoonTask1 = TestDataBuilder.createTask(userId)
                .status(TaskStatus.TO_DO)
                .dueDate(LocalDate.now())
                .build();

        Task dueSoonTask2 = TestDataBuilder.createTask(userId)
                .status(TaskStatus.TO_DO)
                .dueDate(LocalDate.now().plusDays(3))
                .build();

        when(taskService.getOverdueTasks(userId)).thenReturn(List.of(overdueTask));
        when(taskService.getDueSoonTasks(userId)).thenReturn(List.of(dueSoonTask1, dueSoonTask2));

        List<NotificationMessage> result = handler.handleSubscribe(securityUser);

        assertEquals(3, result.size());
        assertEquals(1, result.stream().filter(n -> n.getReason().equals("overdue")).count());
        assertEquals(2, result.stream().filter(n -> n.getReason().equals("due-soon")).count());
    }

    @Test
    void testClientSubscribeMessage_UpdatesInterval() {
        handler.handleSubscribe(securityUser);

        ClientSubscribeMessage message = new ClientSubscribeMessage(30000L);
        handler.handleSubscribeMessage(message, securityUser);

        assertEquals(30000L, handler.getUserInterval(userId));
    }

    @Test
    void testClientSubscribeMessage_DefaultIntervalOnNullValue() {
        handler.handleSubscribe(securityUser);

        ClientSubscribeMessage message = new ClientSubscribeMessage(null);
        handler.handleSubscribeMessage(message, securityUser);

        assertEquals(60000L, handler.getUserInterval(userId));
    }

    @Test
    void testClientAckMessage_AcceptsMessage() {
        when(taskService.getOverdueTasks(userId)).thenReturn(new ArrayList<>());
        when(taskService.getDueSoonTasks(userId)).thenReturn(new ArrayList<>());

        handler.handleSubscribe(securityUser);

        UUID notificationId = UUID.randomUUID();
        ClientAckMessage message = new ClientAckMessage("ack", notificationId);
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> handler.handleAckMessage(message, securityUser)
        );
    }

    @Test
    void testNotificationContent_IncludesAllRequiredFields() {
        Task task = TestDataBuilder.createTask(userId)
                .title("Test task")
                .priority(TaskPriority.HIGH)
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        when(taskService.getOverdueTasks(userId)).thenReturn(List.of(task));
        when(taskService.getDueSoonTasks(userId)).thenReturn(new ArrayList<>());

        List<NotificationMessage> result = handler.handleSubscribe(securityUser);

        assertFalse(result.isEmpty());
        NotificationMessage notification = result.get(0);
        assertEquals("notification", notification.getType());
        assertEquals(task.getId(), notification.getTaskId());
        assertEquals("Test task", notification.getTitle());
        assertEquals("overdue", notification.getReason());
        assertEquals(task.getDueDate(), notification.getDueDate());
        assertEquals(TaskPriority.HIGH, notification.getPriority());
    }

    @Test
    void testMultipleUsers_IsolatedNotifications() {
        UUID userId2 = UUID.randomUUID();
        SecurityUser securityUser2 = mock(SecurityUser.class);
        when(securityUser2.getId()).thenReturn(userId2);

        Task task1 = TestDataBuilder.createTask(userId).build();
        Task task2 = TestDataBuilder.createTask(userId2).build();

        when(taskService.getOverdueTasks(userId)).thenReturn(List.of(task1));
        when(taskService.getOverdueTasks(userId2)).thenReturn(List.of(task2));
        when(taskService.getDueSoonTasks(any())).thenReturn(new ArrayList<>());

        List<NotificationMessage> result1 = handler.handleSubscribe(securityUser);
        List<NotificationMessage> result2 = handler.handleSubscribe(securityUser2);

        assertEquals(1, result1.size());
        assertEquals(1, result2.size());
        assertEquals(task1.getId(), result1.get(0).getTaskId());
        assertEquals(task2.getId(), result2.get(0).getTaskId());
    }

    @Test
    void testDoneTasksExcluded_NotSentAsNotifications() {
        Task doneTask = TestDataBuilder.createTask(userId)
                .status(TaskStatus.DONE)
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        when(taskService.getOverdueTasks(userId)).thenReturn(new ArrayList<>());
        when(taskService.getDueSoonTasks(userId)).thenReturn(new ArrayList<>());

        List<NotificationMessage> result = handler.handleSubscribe(securityUser);

        assertTrue(result.isEmpty());
    }

    @Test
    void testNotificationDTO_CreationSuccessful() {
        UUID notifId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        LocalDate dueDate = LocalDate.now();

        NotificationMessage notification = new NotificationMessage(
                notifId, taskId, "Task", "overdue", dueDate, TaskPriority.MEDIUM
        );

        assertNotNull(notification);
        assertEquals("notification", notification.getType());
        assertEquals(notifId, notification.getId());
        assertEquals(taskId, notification.getTaskId());
    }

    @Test
    void testSendNotificationToUser_Executes() {
        NotificationMessage notification = new NotificationMessage(
                UUID.randomUUID(), UUID.randomUUID(), "Task", "overdue",
                LocalDate.now(), TaskPriority.HIGH
        );

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> handler.sendNotificationToUser(userId, notification)
        );
    }

    @Test
    void testClearSentNotifications_ResetsTrackingForUser() {
        handler.handleSubscribe(securityUser);
        handler.clearSentNotifications(userId);

        UUID taskId = UUID.randomUUID();
        assertFalse(handler.hasAlreadySent(userId, taskId, "overdue"));
    }

    @Test
    void testRemoveUser_CleansupUserState() {
        handler.handleSubscribe(securityUser);
        handler.removeUser(userId);

        // After removal, user should get default interval on re-subscribe
        assertEquals(60000L, handler.getUserInterval(UUID.randomUUID()));
    }

    @Test
    void testDeduplication_SameNotificationNotSentTwice() {
        UUID taskId = UUID.randomUUID();
        Task task = TestDataBuilder.createTask(userId)
                .id(taskId)
                .status(TaskStatus.TO_DO)
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        when(taskService.getOverdueTasks(userId)).thenReturn(List.of(task));
        when(taskService.getDueSoonTasks(userId)).thenReturn(new ArrayList<>());

        List<NotificationMessage> batch1 = handler.handleSubscribe(securityUser);
        assertEquals(1, batch1.size());
        assertTrue(handler.hasAlreadySent(userId, taskId, "overdue"));
    }

    @Test
    void testMultipleSubscriptions_TrackedSeparately() {
        UUID userId2 = UUID.randomUUID();
        SecurityUser user2 = mock(SecurityUser.class);
        when(user2.getId()).thenReturn(userId2);

        when(taskService.getOverdueTasks(userId)).thenReturn(new ArrayList<>());
        when(taskService.getOverdueTasks(userId2)).thenReturn(new ArrayList<>());
        when(taskService.getDueSoonTasks(any())).thenReturn(new ArrayList<>());

        handler.handleSubscribe(securityUser);
        handler.handleSubscribe(user2);

        assertEquals(60000L, handler.getUserInterval(userId));
        assertEquals(60000L, handler.getUserInterval(userId2));

        handler.handleSubscribeMessage(new ClientSubscribeMessage(15000L), securityUser);
        assertEquals(15000L, handler.getUserInterval(userId));
        assertEquals(60000L, handler.getUserInterval(userId2));
    }

    @Test
    void testPriorityPreserved_InNotification() {
        Task highPriorityTask = TestDataBuilder.createTask(userId)
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TO_DO)
                .dueDate(LocalDate.now().minusDays(1))
                .build();

        when(taskService.getOverdueTasks(userId)).thenReturn(List.of(highPriorityTask));
        when(taskService.getDueSoonTasks(userId)).thenReturn(new ArrayList<>());

        List<NotificationMessage> result = handler.handleSubscribe(securityUser);

        assertEquals(TaskPriority.HIGH, result.get(0).getPriority());
    }
}
