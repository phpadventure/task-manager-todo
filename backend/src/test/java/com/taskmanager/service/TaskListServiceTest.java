package com.taskmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taskmanager.dto.TaskListDTO;
import com.taskmanager.entity.TaskList;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.TaskListRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.security.SecurityUser;
import com.taskmanager.util.TestDataBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskListServiceTest {
  @Mock
  private TaskListRepository taskListRepository;

  @Mock
  private TaskRepository taskRepository;

  @InjectMocks
  private TaskListService taskListService;

  private UUID userId;
  private SecurityUser securityUser;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    User user = TestDataBuilder.createTestUser(userId, "test@example.com");
    securityUser = new SecurityUser(user);
  }

  private void mockSecurityContext() {
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(securityUser);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void testCreateList_ValidInput_CreatesListAndReturnsDTO() {
    mockSecurityContext();
    String title = "Groceries";
    TaskList savedList = TestDataBuilder.createTestTaskList(userId, title);

    when(taskListRepository.save(any(TaskList.class))).thenReturn(savedList);

    TaskListDTO result = taskListService.createList(userId, title);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo(title);
    verify(taskListRepository).save(any(TaskList.class));
  }

  @Test
  void testGetListsByUser_WithExistingLists_ReturnsAllUserLists() {
    mockSecurityContext();
    TaskList list1 = TestDataBuilder.createTestTaskList(userId, "List 1");
    TaskList list2 = TestDataBuilder.createTestTaskList(userId, "List 2");

    when(taskListRepository.findByUserId(userId)).thenReturn(List.of(list1, list2));

    List<TaskListDTO> result = taskListService.getListsByUser(userId);

    assertThat(result).hasSize(2);
  }

  @Test
  void testGetListById_UserOwnsList_ReturnsDTO() {
    mockSecurityContext();
    UUID listId = UUID.randomUUID();
    TaskList taskList = TestDataBuilder.createTestTaskList(listId, userId, "My List");

    when(taskListRepository.findByIdAndUserId(listId, userId)).thenReturn(Optional.of(taskList));

    TaskListDTO result = taskListService.getListById(listId, userId);

    assertThat(result).isNotNull();
    assertThat(result.getTitle()).isEqualTo("My List");
  }

  @Test
  void testGetListById_UserDoesNotOwnList_ThrowsResourceNotFoundException() {
    mockSecurityContext();
    UUID listId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();

    when(taskListRepository.findByIdAndUserId(listId, otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskListService.getListById(listId, otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void testUpdateList_UserOwnsList_UpdatesAndReturnsDTO() {
    mockSecurityContext();
    UUID listId = UUID.randomUUID();
    TaskList taskList = TestDataBuilder.createTestTaskList(listId, userId, "Old Title");

    when(taskListRepository.findByIdAndUserId(listId, userId)).thenReturn(Optional.of(taskList));
    when(taskListRepository.save(any(TaskList.class))).thenReturn(taskList);

    TaskListDTO result = taskListService.updateList(listId, userId, "New Title");

    assertThat(result).isNotNull();
    verify(taskListRepository).save(any(TaskList.class));
  }

  @Test
  void testUpdateList_UserDoesNotOwnList_ThrowsResourceNotFoundException() {
    mockSecurityContext();
    UUID listId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();

    when(taskListRepository.findByIdAndUserId(listId, otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskListService.updateList(listId, otherUserId, "New Title"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void testDeleteList_UserOwnsList_DeletesSuccessfully() {
    mockSecurityContext();
    UUID listId = UUID.randomUUID();
    TaskList taskList = TestDataBuilder.createTestTaskList(listId, userId, "To Delete");

    when(taskListRepository.findByIdAndUserId(listId, userId)).thenReturn(Optional.of(taskList));
    when(taskRepository.countByListId(listId)).thenReturn(0);

    taskListService.deleteList(listId, userId);

    verify(taskListRepository).deleteById(listId);
  }

  @Test
  void testDeleteList_ListHasTasks_ThrowsConflictException() {
    mockSecurityContext();
    UUID listId = UUID.randomUUID();
    TaskList taskList = TestDataBuilder.createTestTaskList(listId, userId, "List with Tasks");

    when(taskListRepository.findByIdAndUserId(listId, userId)).thenReturn(Optional.of(taskList));
    when(taskRepository.countByListId(listId)).thenReturn(2);

    assertThatThrownBy(() -> taskListService.deleteList(listId, userId))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("Cannot delete list with tasks");
  }

  @Test
  void testDeleteList_UserDoesNotOwnList_ThrowsResourceNotFoundException() {
    mockSecurityContext();
    UUID listId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();

    when(taskListRepository.findByIdAndUserId(listId, otherUserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> taskListService.deleteList(listId, otherUserId))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
