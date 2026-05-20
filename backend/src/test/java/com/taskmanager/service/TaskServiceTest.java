package com.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.dto.UpdateTaskRequest;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskList;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.TaskListRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock private TaskRepository taskRepository;
  @Mock private TaskListRepository taskListRepository;

  @InjectMocks private TaskService taskService;

  private UUID userId;
  private UUID listId;
  private UUID taskId;
  private TaskList taskList;
  private Task task;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    listId = UUID.randomUUID();
    taskId = UUID.randomUUID();
    taskList = TestDataBuilder.createTestList(userId, "Test List");
    taskList.setId(listId);
    task = TestDataBuilder.createTestTask(listId, userId, "Test Task", TaskStatus.TO_DO,
        TaskPriority.MEDIUM);
    task.setId(taskId);
  }

  @Test
  void testCreateTask_ValidInput_ShouldReturnTask() {
    CreateTaskRequest request = new CreateTaskRequest();
    request.setTitle("New Task");
    request.setDescription("Task description");

    when(taskListRepository.findByIdAndUserId(listId, userId))
        .thenReturn(Optional.of(taskList));

    ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
    when(taskRepository.save(taskCaptor.capture())).thenAnswer(
        invocation -> {
          Task savedTask = taskCaptor.getValue();
          savedTask.setId(taskId);
          return savedTask;
        });

    Task result = taskService.createTask(userId, listId, request);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("New Task");
    assertThat(result.getUserId()).isEqualTo(userId);
    verify(taskRepository).save(any(Task.class));
  }

  @Test
  void testCreateTask_ListNotFound_ShouldThrowException() {
    CreateTaskRequest request = new CreateTaskRequest();
    request.setTitle("New Task");

    when(taskListRepository.findByIdAndUserId(listId, userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.createTask(userId, listId, request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("List");
  }

  @Test
  void testGetTask_ValidIdAndUser_ShouldReturnTask() {
    when(taskRepository.findByIdAndUserId(taskId, userId))
        .thenReturn(Optional.of(task));

    Task result = taskService.getTask(taskId, userId);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(taskId);
    assertThat(result.getUserId()).isEqualTo(userId);
  }

  @Test
  void testGetTask_TaskNotFound_ShouldThrowException() {
    when(taskRepository.findByIdAndUserId(taskId, userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.getTask(taskId, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Task");
  }

  @Test
  void testGetTasksByList_ValidListAndUser_ShouldReturnTasks() {
    List<Task> tasks = List.of(task);

    when(taskListRepository.findByIdAndUserId(listId, userId))
        .thenReturn(Optional.of(taskList));
    when(taskRepository.findByListIdAndUserId(listId, userId))
        .thenReturn(tasks);

    List<Task> result = taskService.getTasksByList(listId, userId);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(taskId);
  }

  @Test
  void testGetTasksByList_ListNotFound_ShouldThrowException() {
    when(taskListRepository.findByIdAndUserId(listId, userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.getTasksByList(listId, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("List");
  }

  @Test
  void testGetAllTasksForUser_ShouldReturnAllUserTasks() {
    List<Task> tasks = List.of(task);

    when(taskRepository.findByUserId(userId)).thenReturn(tasks);

    List<Task> result = taskService.getAllTasksForUser(userId);

    assertThat(result).hasSize(1);
  }

  @Test
  void testUpdateTask_ValidInput_ShouldUpdateTask() {
    UpdateTaskRequest request = new UpdateTaskRequest();
    request.setTitle("Updated Title");
    request.setStatus(TaskStatus.IN_PROGRESS);

    when(taskRepository.findByIdAndUserId(taskId, userId))
        .thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenReturn(task);

    Task result = taskService.updateTask(taskId, userId, request);

    assertThat(result).isNotNull();
    verify(taskRepository).save(any(Task.class));
  }

  @Test
  void testUpdateTask_TaskNotFound_ShouldThrowException() {
    UpdateTaskRequest request = new UpdateTaskRequest();
    request.setTitle("Updated Title");

    when(taskRepository.findByIdAndUserId(taskId, userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.updateTask(taskId, userId, request))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Task");
  }

  @Test
  void testUpdateTaskStatus_ValidStatus_ShouldUpdate() {
    when(taskRepository.findByIdAndUserId(taskId, userId))
        .thenReturn(Optional.of(task));
    when(taskRepository.save(any(Task.class))).thenReturn(task);

    Task result = taskService.updateTaskStatus(taskId, userId, TaskStatus.DONE);

    assertThat(result).isNotNull();
    verify(taskRepository).save(any(Task.class));
  }

  @Test
  void testDeleteTask_ValidId_ShouldDelete() {
    when(taskRepository.findByIdAndUserId(taskId, userId))
        .thenReturn(Optional.of(task));

    taskService.deleteTask(taskId, userId);

    verify(taskRepository).delete(task);
  }

  @Test
  void testDeleteTask_TaskNotFound_ShouldThrowException() {
    when(taskRepository.findByIdAndUserId(taskId, userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskService.deleteTask(taskId, userId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Task");
  }

  @Test
  void testSearchTasks_WithQuery_ShouldReturnMatches() {
    List<Task> tasks = List.of(task);

    when(taskRepository.searchByTitleOrDescription(userId, "Test"))
        .thenReturn(tasks);

    List<Task> result = taskService.searchTasks(userId, "Test");

    assertThat(result).hasSize(1);
  }

  @Test
  void testSearchTasks_EmptyQuery_ShouldReturnAllTasks() {
    List<Task> tasks = List.of(task);

    when(taskRepository.findByUserId(userId)).thenReturn(tasks);

    List<Task> result = taskService.searchTasks(userId, "");

    assertThat(result).hasSize(1);
  }

  @Test
  void testFilterByStatus_ShouldReturnFilteredTasks() {
    List<Task> tasks = List.of(task);

    when(taskRepository.findByUserIdAndStatus(userId, TaskStatus.TO_DO))
        .thenReturn(tasks);

    List<Task> result = taskService.filterByStatus(userId, TaskStatus.TO_DO);

    assertThat(result).hasSize(1);
  }

  @Test
  void testFilterByPriority_ShouldReturnFilteredTasks() {
    List<Task> tasks = List.of(task);

    when(taskRepository.findByUserIdAndPriority(userId, TaskPriority.HIGH))
        .thenReturn(tasks);

    List<Task> result = taskService.filterByPriority(userId, TaskPriority.HIGH);

    assertThat(result).hasSize(1);
  }

  @Test
  void testGetOverdueTasks_ShouldReturnTasks() {
    List<Task> tasks = List.of(task);

    when(taskRepository.findOverdueTasks(userId, TaskStatus.DONE)).thenReturn(tasks);

    List<Task> result = taskService.getOverdueTasks(userId);

    assertThat(result).hasSize(1);
  }

  @Test
  void testGetDueSoonTasks_ShouldReturnTasks() {
    List<Task> tasks = List.of(task);

    when(taskRepository.findDueSoonTasks(any(UUID.class), any(LocalDate.class), any(TaskStatus.class)))
        .thenReturn(tasks);

    List<Task> result = taskService.getDueSoonTasks(userId);

    assertThat(result).hasSize(1);
  }
}
