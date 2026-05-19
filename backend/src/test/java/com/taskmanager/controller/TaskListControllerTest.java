package com.taskmanager.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.dto.CreateTaskListRequest;
import com.taskmanager.dto.TaskListDTO;
import com.taskmanager.dto.UpdateTaskListRequest;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.UnauthorizedException;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.service.TaskListService;
import com.taskmanager.util.TestDataBuilder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TaskListController.class)
class TaskListControllerTest {

  @TestConfiguration
  static class TestSecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.csrf(csrf -> csrf.disable())
          .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
      return http.build();
    }
  }

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TaskListService taskListService;

  private final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new JavaTimeModule());

  private UUID userId;
  private UUID listId;
  private UsernamePasswordAuthenticationToken authPrincipal;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    listId = UUID.randomUUID();
    User user = TestDataBuilder.createTestUser(userId, "test@example.com");
    SecurityUser securityUser = new SecurityUser(user);
    authPrincipal =
        new UsernamePasswordAuthenticationToken(
            securityUser, null, securityUser.getAuthorities());

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authPrincipal);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void testGetLists_AuthenticatedUser_Returns200() throws Exception {
    TaskListDTO list1 = new TaskListDTO(listId, "List 1", Instant.now(), Instant.now());
    when(taskListService.getListsByUser(userId)).thenReturn(List.of(list1));

    mockMvc.perform(
            get("/lists").contentType(MediaType.APPLICATION_JSON).principal(authPrincipal))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("List 1"));
  }

  @Test
  void testGetList_UserOwnsList_Returns200() throws Exception {
    TaskListDTO list = new TaskListDTO(listId, "My List", Instant.now(), Instant.now());
    when(taskListService.getListById(listId, userId)).thenReturn(list);

    mockMvc.perform(
            get("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("My List"));
  }

  @Test
  void testGetList_ListNotFound_Returns404() throws Exception {
    when(taskListService.getListById(listId, userId))
        .thenThrow(new ResourceNotFoundException("TaskList", listId));

    mockMvc.perform(
            get("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetList_UserDoesNotOwnList_Returns403() throws Exception {
    when(taskListService.getListById(listId, userId))
        .thenThrow(new UnauthorizedException("You do not have access to this resource"));

    mockMvc.perform(
            get("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void testCreateList_ValidInput_Returns201() throws Exception {
    CreateTaskListRequest request = new CreateTaskListRequest("New List");
    TaskListDTO created = new TaskListDTO(listId, "New List", Instant.now(), Instant.now());
    when(taskListService.createList(eq(userId), eq("New List"))).thenReturn(created);

    mockMvc.perform(
            post("/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("New List"));
  }

  @Test
  void testCreateList_BlankTitle_Returns400() throws Exception {
    CreateTaskListRequest request = new CreateTaskListRequest("");

    mockMvc.perform(
            post("/lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isBadRequest());
  }

  @Test
  void testUpdateList_UserOwnsList_Returns200() throws Exception {
    UpdateTaskListRequest request = new UpdateTaskListRequest("Updated List");
    TaskListDTO updated = new TaskListDTO(listId, "Updated List", Instant.now(), Instant.now());
    when(taskListService.updateList(eq(listId), eq(userId), eq("Updated List")))
        .thenReturn(updated);

    mockMvc.perform(
            put("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Updated List"));
  }

  @Test
  void testUpdateList_UserDoesNotOwnList_Returns403() throws Exception {
    UpdateTaskListRequest request = new UpdateTaskListRequest("Updated List");
    when(taskListService.updateList(eq(listId), eq(userId), eq("Updated List")))
        .thenThrow(new UnauthorizedException("You do not have access to this resource"));

    mockMvc.perform(
            put("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isForbidden());
  }

  @Test
  void testDeleteList_UserOwnsList_Returns204() throws Exception {
    doNothing().when(taskListService).deleteList(eq(listId), eq(userId));

    mockMvc.perform(
            delete("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNoContent());
  }

  @Test
  void testDeleteList_ListHasTasks_Returns409() throws Exception {
    doThrow(
            new ConflictException(
                "Cannot delete list with tasks. Delete all tasks first."))
        .when(taskListService)
        .deleteList(eq(listId), eq(userId));

    mockMvc.perform(
            delete("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isConflict());
  }

  @Test
  void testDeleteList_UserDoesNotOwnList_Returns403() throws Exception {
    doThrow(new UnauthorizedException("You do not have access to this resource"))
        .when(taskListService)
        .deleteList(eq(listId), eq(userId));

    mockMvc.perform(
            delete("/lists/{id}", listId)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden());
  }
}
