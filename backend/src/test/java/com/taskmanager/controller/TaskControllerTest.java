package com.taskmanager.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.dto.CreateTaskRequest;
import com.taskmanager.dto.UpdateTaskRequest;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.service.SecurityContextService;
import com.taskmanager.service.TaskService;
import com.taskmanager.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaskController.class)
class TaskControllerTest {

  @TestConfiguration
  static class TestSecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
      return http.build();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TaskService taskService;

  @MockitoBean private SecurityContextService securityContextService;

  private final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new JavaTimeModule());

  private UUID userId;
  private UUID listId;
  private UUID taskId;
  private Task task;
  private UsernamePasswordAuthenticationToken authPrincipal;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    listId = UUID.randomUUID();
    taskId = UUID.randomUUID();
    task = TestDataBuilder.createTestTask(listId, userId, "Test Task");
    task.setId(taskId);

    when(securityContextService.getCurrentUserId()).thenReturn(userId);

    User user = TestDataBuilder.createTestUser(userId, "test@example.com");
    SecurityUser securityUser = new SecurityUser(user);
    authPrincipal =
        new UsernamePasswordAuthenticationToken(
            securityUser, null, securityUser.getAuthorities());
  }

  @Test
  void testGetAllTasks_NoFilters_ShouldReturnOk() throws Exception {
    List<Task> tasks = List.of(task);
    when(taskService.getAllTasksForUser(userId)).thenReturn(tasks);

    mockMvc
        .perform(get("/tasks").principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(taskId.toString()));
  }

  @Test
  void testGetAllTasks_FilterByStatus_ShouldReturnFiltered() throws Exception {
    List<Task> tasks = List.of(task);
    when(taskService.filterByStatus(userId, TaskStatus.TO_DO)).thenReturn(tasks);

    mockMvc
        .perform(get("/tasks").param("status", "TO_DO").principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testSearchTasks_ValidQuery_ShouldReturnMatches() throws Exception {
    List<Task> tasks = List.of(task);
    when(taskService.searchTasks(userId, "Test")).thenReturn(tasks);

    mockMvc
        .perform(get("/tasks/search").param("q", "Test").principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testGetOverdueTasks_ShouldReturnOverdueTasks() throws Exception {
    List<Task> tasks = List.of(task);
    when(taskService.getOverdueTasks(userId)).thenReturn(tasks);

    mockMvc
        .perform(get("/tasks/overdue").principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testGetDueSoonTasks_ShouldReturnDueSoonTasks() throws Exception {
    List<Task> tasks = List.of(task);
    when(taskService.getDueSoonTasks(userId)).thenReturn(tasks);

    mockMvc
        .perform(get("/tasks/due-soon").principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testGetTasksByList_ValidListId_ShouldReturnTasks() throws Exception {
    List<Task> tasks = List.of(task);
    when(taskService.getTasksByList(listId, userId)).thenReturn(tasks);

    mockMvc
        .perform(get("/lists/{listId}/tasks", listId).principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testGetTasksByList_ListNotFound_ShouldReturnNotFound() throws Exception {
    when(taskService.getTasksByList(listId, userId))
        .thenThrow(new ResourceNotFoundException("List", listId));

    mockMvc
        .perform(get("/lists/{listId}/tasks", listId).principal(authPrincipal))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetTaskInList_ValidIds_ShouldReturnOk() throws Exception {
    when(taskService.getTaskInList(taskId, listId, userId)).thenReturn(task);

    mockMvc
        .perform(get("/lists/{listId}/tasks/{taskId}", listId, taskId).principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(taskId.toString()));
  }

  @Test
  void testGetTaskInList_TaskNotInList_ShouldReturnUnauthorized() throws Exception {
    when(taskService.getTaskInList(taskId, listId, userId))
        .thenThrow(new UnauthorizedException("Task does not belong to this list"));

    mockMvc
        .perform(get("/lists/{listId}/tasks/{taskId}", listId, taskId).principal(authPrincipal))
        .andExpect(status().isForbidden());
  }

  @Test
  void testCreateTask_ValidRequest_ShouldReturnCreated() throws Exception {
    CreateTaskRequest request = new CreateTaskRequest();
    request.setTitle("New Task");
    request.setDescription("Description");

    when(taskService.createTask(eq(userId), eq(listId), any(CreateTaskRequest.class)))
        .thenReturn(task);

    mockMvc
        .perform(
            post("/lists/{listId}/tasks", listId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
                .principal(authPrincipal))
        .andExpect(status().isCreated());
  }

  @Test
  void testCreateTask_BlankTitle_ShouldReturnBadRequest() throws Exception {
    CreateTaskRequest request = new CreateTaskRequest();
    request.setTitle("");

    mockMvc
        .perform(
            post("/lists/{listId}/tasks", listId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
                .principal(authPrincipal))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateTask_ListNotFound_ShouldReturnNotFound() throws Exception {
    CreateTaskRequest request = new CreateTaskRequest();
    request.setTitle("New Task");

    when(taskService.createTask(eq(userId), eq(listId), any(CreateTaskRequest.class)))
        .thenThrow(new ResourceNotFoundException("List", listId));

    mockMvc
        .perform(
            post("/lists/{listId}/tasks", listId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void testUpdateTaskInList_ValidRequest_ShouldReturnOk() throws Exception {
    UpdateTaskRequest request = new UpdateTaskRequest();
    request.setTitle("Updated Task");

    when(taskService.getTaskInList(taskId, listId, userId)).thenReturn(task);
    when(taskService.updateTask(eq(taskId), eq(userId), any(UpdateTaskRequest.class)))
        .thenReturn(task);

    mockMvc
        .perform(
            put("/lists/{listId}/tasks/{taskId}", listId, taskId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
                .principal(authPrincipal))
        .andExpect(status().isOk());
  }

  @Test
  void testUpdateTaskInList_TaskNotInList_ShouldReturnUnauthorized() throws Exception {
    UpdateTaskRequest request = new UpdateTaskRequest();
    request.setTitle("Updated Task");

    when(taskService.getTaskInList(taskId, listId, userId))
        .thenThrow(new UnauthorizedException("Task does not belong to this list"));

    mockMvc
        .perform(
            put("/lists/{listId}/tasks/{taskId}", listId, taskId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
                .principal(authPrincipal))
        .andExpect(status().isForbidden());
  }

  @Test
  void testUpdateTaskStatus_ValidStatus_ShouldReturnOk() throws Exception {
    when(taskService.updateTaskStatus(taskId, userId, TaskStatus.DONE))
        .thenReturn(task);

    mockMvc
        .perform(
            patch("/tasks/{taskId}/status", taskId)
                .param("status", "DONE")
                .principal(authPrincipal))
        .andExpect(status().isOk());
  }

  @Test
  void testDeleteTaskInList_ValidIds_ShouldReturnNoContent() throws Exception {
    when(taskService.getTaskInList(taskId, listId, userId)).thenReturn(task);
    doNothing().when(taskService).deleteTask(taskId, userId);

    mockMvc
        .perform(delete("/lists/{listId}/tasks/{taskId}", listId, taskId).principal(authPrincipal))
        .andExpect(status().isNoContent());

    verify(taskService).deleteTask(taskId, userId);
  }

  @Test
  void testDeleteTaskInList_TaskNotInList_ShouldReturnUnauthorized() throws Exception {
    when(taskService.getTaskInList(taskId, listId, userId))
        .thenThrow(new UnauthorizedException("Task does not belong to this list"));

    mockMvc
        .perform(delete("/lists/{listId}/tasks/{taskId}", listId, taskId).principal(authPrincipal))
        .andExpect(status().isForbidden());
  }
}
