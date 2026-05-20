package com.taskmanager.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.dto.UpdateTaskRequest;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskList;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.repository.TaskListRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskListRepository taskListRepository;

  @Value("${app.task.due-soon-days:3}")
  private int dueSoonDays;

  public Task createTask(UUID userId, UUID listId, CreateTaskRequest request) {
    TaskList list =
        taskListRepository
            .findByIdAndUserId(listId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("List", listId));

    Task task = new Task();
    task.setListId(list.getId());
    task.setUserId(userId);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(
        request.getStatus() != null ? request.getStatus() : TaskStatus.TO_DO);
    task.setPriority(
        request.getPriority() != null ? request.getPriority()
            : TaskPriority.MEDIUM);
    task.setDueDate(request.getDueDate());
    task.setCreatedAt(Instant.now());
    task.setUpdatedAt(Instant.now());

    return taskRepository.save(task);
  }

  public Task getTask(UUID taskId, UUID userId) {
    return taskRepository
        .findByIdAndUserId(taskId, userId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Task", taskId));
  }

  public List<Task> getTasksByList(UUID listId, UUID userId) {
    TaskList list =
        taskListRepository
            .findByIdAndUserId(listId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("List", listId));

    return taskRepository.findByListIdAndUserId(list.getId(), userId);
  }

  public List<Task> getAllTasksForUser(UUID userId) {
    return taskRepository.findByUserId(userId);
  }

  public Task updateTask(
      UUID taskId, UUID userId, UpdateTaskRequest request) {
    Task task =
        taskRepository
            .findByIdAndUserId(taskId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Task", taskId));

    if (request.getTitle() != null) {
      task.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      task.setDescription(request.getDescription());
    }
    if (request.getStatus() != null) {
      task.setStatus(request.getStatus());
    }
    if (request.getPriority() != null) {
      task.setPriority(request.getPriority());
    }
    if (request.getDueDate() != null) {
      task.setDueDate(request.getDueDate());
    }

    task.setUpdatedAt(Instant.now());
    return taskRepository.save(task);
  }

  public Task updateTaskStatus(
      UUID taskId, UUID userId, TaskStatus newStatus) {
    Task task =
        taskRepository
            .findByIdAndUserId(taskId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Task", taskId));

    task.setStatus(newStatus);
    task.setUpdatedAt(Instant.now());
    return taskRepository.save(task);
  }

  public void deleteTask(UUID taskId, UUID userId) {
    Task task =
        taskRepository
            .findByIdAndUserId(taskId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Task", taskId));

    taskRepository.delete(task);
  }

  public List<Task> searchTasks(UUID userId, String query) {
    if (query == null || query.isEmpty()) {
      return getAllTasksForUser(userId);
    }
    return taskRepository.searchByTitleOrDescription(userId, query);
  }

  public List<Task> filterByStatus(UUID userId, TaskStatus status) {
    return taskRepository.findByUserIdAndStatus(userId, status);
  }

  public List<Task> filterByPriority(UUID userId, TaskPriority priority) {
    return taskRepository.findByUserIdAndPriority(userId, priority);
  }

  public List<Task> getOverdueTasks(UUID userId) {
    return taskRepository.findOverdueTasks(userId, TaskStatus.DONE);
  }

  public List<Task> getDueSoonTasks(UUID userId) {
    LocalDate dueSoonDate = LocalDate.now().plusDays(dueSoonDays);
    return taskRepository.findDueSoonTasks(userId, dueSoonDate, TaskStatus.DONE);
  }
}
