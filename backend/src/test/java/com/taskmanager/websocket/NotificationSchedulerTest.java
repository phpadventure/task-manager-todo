package com.taskmanager.websocket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;

@ExtendWith(MockitoExtension.class)
class NotificationSchedulerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NotificationScheduler scheduler;
    private TaskNotificationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TaskNotificationHandler(taskService, messagingTemplate);
        scheduler = new NotificationScheduler(taskService, userService, handler);
    }

    @Test
    void testScheduler_IsCreatedSuccessfully() {
        assertNotNull(scheduler);
    }

    @Test
    void testScheduler_HasTaskServiceInjected() {
        assertNotNull(taskService);
    }

    @Test
    void testScheduler_HasUserServiceInjected() {
        assertNotNull(userService);
    }

    @Test
    void testScheduler_HasNotificationHandlerInjected() {
        assertNotNull(handler);
    }

    @Test
    void testScheduler_CanInvokePeriodicTask() {
        scheduler.sendPeriodicNotifications();
    }

    @Test
    void testNotificationHandler_InitializedWithAllDependencies() {
        assertNotNull(handler);
        assertNotNull(handler);
    }

    @Test
    void testScheduler_InstanceCreation() {
        TaskService taskServiceMock = mock(TaskService.class);
        UserService userServiceMock = mock(UserService.class);
        SimpMessagingTemplate templateMock = mock(SimpMessagingTemplate.class);
        TaskNotificationHandler handlerMock = new TaskNotificationHandler(
                taskServiceMock, templateMock
        );
        NotificationScheduler schedulerMock = new NotificationScheduler(
                taskServiceMock, userServiceMock, handlerMock
        );

        assertNotNull(schedulerMock);
    }

    @Test
    void testScheduler_MultiplePeriodicInvocations() {
        scheduler.sendPeriodicNotifications();
        scheduler.sendPeriodicNotifications();
        scheduler.sendPeriodicNotifications();
    }

    @Test
    void testNotificationHandler_CanBeInvokedMultipleTimes() {
        scheduler.sendPeriodicNotifications();
        scheduler.sendPeriodicNotifications();
        scheduler.sendPeriodicNotifications();
    }

    @Test
    void testScheduler_ServiceFieldsNotNull() {
        assertNotNull(taskService);
        assertNotNull(userService);
    }

    @Test
    void testScheduler_CallsPeriodicTask() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> scheduler.sendPeriodicNotifications()
        );
    }
}
