package com.taskmanager.util;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskList;
import com.taskmanager.entity.User;
import com.taskmanager.entity.UserAuthProvider;


public class TestDataBuilder {

  public static User createTestUser() {
    return createTestUser(UUID.randomUUID(), "test@example.com");
  }

  public static User createTestUser(UUID id, String email) {
    User user = new User();
    user.setId(id);
    user.setEmail(email);
    user.setDisplayName("Test User");
    user.setAvatarUrl("https://example.com/avatar.jpg");
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    return user;
  }

  public static User createTestUser(UUID id, String email, String displayName) {
    User user = createTestUser(id, email);
    user.setDisplayName(displayName);
    return user;
  }

  public static UserAuthProvider createTestUserAuthProvider(
      UUID id, UUID userId, String provider) {
    String providerUserId = "provider_user_id_" + UUID.randomUUID();
    return createTestUserAuthProvider(id, userId, provider, providerUserId);
  }

  public static UserAuthProvider createTestUserAuthProvider(
      UUID id, UUID userId, String provider, String providerUserId) {
    UserAuthProvider authProvider = new UserAuthProvider();
    authProvider.setId(id);
    authProvider.setUserId(userId);
    authProvider.setProvider(provider);
    authProvider.setProviderUserId(providerUserId);
    authProvider.setCreatedAt(Instant.now());
    return authProvider;
  }

  public static TaskList createTestTaskList(UUID id, UUID userId, String title) {
    TaskList taskList = new TaskList();
    taskList.setId(id);
    taskList.setUserId(userId);
    taskList.setTitle(title);
    taskList.setCreatedAt(Instant.now());
    taskList.setUpdatedAt(Instant.now());
    return taskList;
  }

  public static TaskList createTestTaskList(UUID userId, String title) {
    return createTestTaskList(UUID.randomUUID(), userId, title);
  }

  public static Task createTestTask(UUID id, UUID listId, UUID userId, String title) {
    Task task = new Task();
    task.setId(id);
    task.setListId(listId);
    task.setUserId(userId);
    task.setTitle(title);
    task.setDescription("Test task description");
    task.setStatus("TO_DO");
    task.setPriority("MEDIUM");
    task.setDueDate(LocalDate.now().plusDays(7));
    task.setCreatedAt(Instant.now());
    task.setUpdatedAt(Instant.now());
    return task;
  }

  public static Task createTestTask(UUID listId, UUID userId, String title) {
    return createTestTask(UUID.randomUUID(), listId, userId, title);
  }

  public static Task createTestTask(UUID listId, UUID userId, String title, String status) {
    Task task = createTestTask(listId, userId, title);
    task.setStatus(status);
    return task;
  }

  public static Task createTestTask(
      UUID listId, UUID userId, String title, String status, String priority) {
    Task task = createTestTask(listId, userId, title, status);
    task.setPriority(priority);
    return task;
  }

  public static Task createTestTask(
      UUID listId, UUID userId, String title, String status, String priority, LocalDate dueDate) {
    Task task = createTestTask(listId, userId, title, status, priority);
    task.setDueDate(dueDate);
    return task;
  }
}
