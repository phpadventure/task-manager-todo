---
name: spring-test-patterns
description: Write unit and integration tests for Java Spring Boot services, controllers, and business logic in the backend directory. Follows established patterns for service layer tests (mocks, assertions,   
  error cases) and controller tests (MockMvc, HTTP status verification).
---

# Java Spring Boot Test Writing Guide

Quick reference for writing unit and integration tests. Two-layer approach: Service tests (logic) + Controller tests (HTTP). Check file reference in last section to find out correct imports for dependencies!

---

## Service Test Pattern

**Location**: `src/test/java/com/taskmanager/service/`

**Tools**: `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`

```java
@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {
  
  @Mock
  private TaskListRepository taskListRepository;

  @InjectMocks
  private TaskListService taskListService;

  @Test
  void testCreateList_ValidInput_CreatesListAndReturnsDTO() {
    // Arrange: Set up mocks and test data
    String title = "Groceries";
    TaskList savedList = TestDataBuilder.createTestTaskList(UUID.randomUUID(), title);
    when(taskListRepository.save(any(TaskList.class))).thenReturn(savedList);

    // Act: Call method under test
    TaskListDTO result = taskListService.createList(UUID.randomUUID(), title);

    // Assert: Verify result and mock calls
    assertThat(result.getTitle()).isEqualTo(title);
    verify(taskListRepository).save(any(TaskList.class));
  }

  @Test
  void testDeleteList_ListHasTasks_ThrowsConflictException() {
    // Arrange: Setup condition that triggers error
    UUID listId = UUID.randomUUID();
    when(taskListRepository.findByIdAndUserId(listId, any())).thenReturn(Optional.of(new TaskList()));
    when(taskRepository.countByListId(listId)).thenReturn(2);

    // Act & Assert: Verify exception thrown
    assertThatThrownBy(() -> taskListService.deleteList(listId, UUID.randomUUID()))
        .isInstanceOf(ConflictException.class)
        .hasMessageContaining("Cannot delete");
  }
}
```

**Key Points**:
- Mock only dependencies (repositories, other services)
- Test happy path + error cases separately
- Use `verify()` to confirm mock was called
- Naming: `test{Method}_{Condition}_{Expected}`

---

## Controller Test Pattern

**Location**: `src/test/java/com/taskmanager/controller/`

**Tools**: `@WebMvcTest`, `MockMvc`, `@MockitoBean`

```java
@WebMvcTest(controllers = TaskListController.class)
class TaskListControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TaskListService taskListService;

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule());

  @Test
  void testGetLists_AuthenticatedUser_Returns200() throws Exception {
    // Arrange: Mock service response
    TaskListDTO list = new TaskListDTO(UUID.randomUUID(), "List 1", Instant.now(), Instant.now());
    when(taskListService.getListsByUser(any())).thenReturn(List.of(list));

    // Act & Assert: Make HTTP request and verify response
    mockMvc.perform(get("/api/lists").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("List 1"));
  }

  @Test
  void testCreateList_ValidInput_Returns201() throws Exception {
    // Arrange: Mock service
    TaskListDTO created = new TaskListDTO(UUID.randomUUID(), "New List", Instant.now(), Instant.now());
    when(taskListService.createList(any(), eq("New List"))).thenReturn(created);

    CreateTaskListRequest request = new CreateTaskListRequest("New List");

    // Act & Assert: POST with JSON body, expect 201
    mockMvc.perform(post("/api/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("New List"));
  }

  @Test
  void testGetList_ServiceThrowsException_ReturnsErrorStatus() throws Exception {
    // Any exception from service maps to HTTP error via GlobalExceptionHandler
    when(taskListService.getListById(any(), any()))
        .thenThrow(new UnauthorizedException("No access"));

    mockMvc.perform(get("/api/lists/123"))
        .andExpect(status().isForbidden());
  }
}
```

**Key Points**:
- Use `@MockitoBean` for service layer
- Test HTTP status via `status().isXxx()`
- Verify response with `jsonPath()`
- Exceptions auto-map to HTTP status via `GlobalExceptionHandler`

---

## Common Assertions

```java
assertThat(result).isNotNull();
assertThat(result.getTitle()).isEqualTo("Groceries");
assertThat(result).hasSize(2);

assertThatThrownBy(() -> service.delete(id))
    .isInstanceOf(ConflictException.class)
    .hasMessageContaining("Cannot delete");

verify(repository).save(any());
verify(repository, never()).delete(any());
```

---

## Test Structure Template

```java
@Test
void testMethodName_Condition_Expected() {
  // ARRANGE: Setup mocks and test data
  when(mock.method()).thenReturn(value);

  // ACT: Call method under test
  Result result = service.methodUnderTest();

  // ASSERT: Verify result and interactions
  assertThat(result).isEqualTo(expected);
  verify(mock).method();
}
```

---

## Running Tests

```bash
./gradlew test                           # All tests
./gradlew test --tests "*TaskList*"      # Filter by class name
./gradlew test jacocoTestReport          # With coverage
./gradlew test --continuous              # Watch mode
```

---

## Files Reference

- Service test: `src/test/java/com/taskmanager/service/TaskListServiceTest.java`
- Controller test: `src/test/java/com/taskmanager/controller/TaskListControllerTest.java`
- Test data: `src/test/java/com/taskmanager/util/TestDataBuilder.java`
