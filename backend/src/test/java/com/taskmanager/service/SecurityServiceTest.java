package com.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskList;
import com.taskmanager.repository.TaskListRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private TaskListRepository taskListRepository;

  @Mock
  private SecurityContextService securityContextService;

  private SecurityService securityService;

  @BeforeEach
  void setUp() {
    securityService = new SecurityService(taskRepository, taskListRepository, securityContextService);
  }

  @Test
  void testUserOwnsTask_WithOwnedTask_ReturnsTrue() {
    UUID userId = UUID.randomUUID();
    UUID taskId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    Task task = TestDataBuilder.createTestTask(listId, userId, "Test task");

    when(securityContextService.getCurrentUserId()).thenReturn(userId);
    when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

    boolean result = securityService.userOwnsTask(taskId);

    assertThat(result).isTrue();
  }

  @Test
  void testUserOwnsTask_WithOtherUsersTask_ReturnsFalse() {
    UUID userId = UUID.randomUUID();
    UUID taskId = UUID.randomUUID();

    when(securityContextService.getCurrentUserId()).thenReturn(userId);
    when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

    boolean result = securityService.userOwnsTask(taskId);

    assertThat(result).isFalse();
  }

  @Test
  void testUserOwnsTask_WithAuthenticationError_ReturnsFalse() {
    UUID taskId = UUID.randomUUID();

    when(securityContextService.getCurrentUserId())
        .thenThrow(new RuntimeException("Not authenticated"));

    boolean result = securityService.userOwnsTask(taskId);

    assertThat(result).isFalse();
  }

  @Test
  void testUserOwnsTaskList_WithOwnedList_ReturnsTrue() {
    UUID userId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    TaskList taskList = TestDataBuilder.createTestTaskList(listId, userId, "Test list");

    when(securityContextService.getCurrentUserId()).thenReturn(userId);
    when(taskListRepository.findByIdAndUserId(listId, userId)).thenReturn(Optional.of(taskList));

    boolean result = securityService.userOwnsTaskList(listId);

    assertThat(result).isTrue();
  }

  @Test
  void testUserOwnsTaskList_WithOtherUsersList_ReturnsFalse() {
    UUID userId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();

    when(securityContextService.getCurrentUserId()).thenReturn(userId);
    when(taskListRepository.findByIdAndUserId(listId, userId)).thenReturn(Optional.empty());

    boolean result = securityService.userOwnsTaskList(listId);

    assertThat(result).isFalse();
  }

  @Test
  void testUserOwnsTaskList_WithAuthenticationError_ReturnsFalse() {
    UUID listId = UUID.randomUUID();

    when(securityContextService.getCurrentUserId())
        .thenThrow(new RuntimeException("Not authenticated"));

    boolean result = securityService.userOwnsTaskList(listId);

    assertThat(result).isFalse();
  }

  @Test
  void testUserOwnsTaskInList_WithCorrectOwnership_ReturnsTrue() {
    UUID userId = UUID.randomUUID();
    UUID taskId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    Task task = TestDataBuilder.createTestTask(listId, userId, "Test task");

    when(securityContextService.getCurrentUserId()).thenReturn(userId);
    when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

    boolean result = securityService.userOwnsTaskInList(taskId, listId);

    assertThat(result).isTrue();
  }

  @Test
  void testUserOwnsTaskInList_WithWrongList_ReturnsFalse() {
    UUID userId = UUID.randomUUID();
    UUID taskId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();
    UUID differentListId = UUID.randomUUID();
    Task task = TestDataBuilder.createTestTask(differentListId, userId, "Test task");

    when(securityContextService.getCurrentUserId()).thenReturn(userId);
    when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.of(task));

    boolean result = securityService.userOwnsTaskInList(taskId, listId);

    assertThat(result).isFalse();
  }

  @Test
  void testUserOwnsTaskInList_WithOtherUsersTask_ReturnsFalse() {
    UUID userId = UUID.randomUUID();
    UUID taskId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();

    when(securityContextService.getCurrentUserId()).thenReturn(userId);
    when(taskRepository.findByIdAndUserId(taskId, userId)).thenReturn(Optional.empty());

    boolean result = securityService.userOwnsTaskInList(taskId, listId);

    assertThat(result).isFalse();
  }

  @Test
  void testUserOwnsTaskInList_WithAuthenticationError_ReturnsFalse() {
    UUID taskId = UUID.randomUUID();
    UUID listId = UUID.randomUUID();

    when(securityContextService.getCurrentUserId())
        .thenThrow(new RuntimeException("Not authenticated"));

    boolean result = securityService.userOwnsTaskInList(taskId, listId);

    assertThat(result).isFalse();
  }
}
