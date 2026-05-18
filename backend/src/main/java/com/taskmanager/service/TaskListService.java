package com.taskmanager.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.taskmanager.dto.TaskListDTO;
import com.taskmanager.entity.TaskList;
import com.taskmanager.exception.ConflictException;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.TaskListRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskListService {
  private final TaskListRepository taskListRepository;
  private final TaskRepository taskRepository;

  public TaskListDTO createList(UUID userId, String title) {
    TaskList taskList = new TaskList();
    taskList.setUserId(userId);
    taskList.setTitle(title);
    taskList.setCreatedAt(Instant.now());
    taskList.setUpdatedAt(Instant.now());
    TaskList saved = taskListRepository.save(taskList);
    return TaskListDTO.fromEntity(saved);
  }

  public List<TaskListDTO> getListsByUser(UUID userId) {
    return taskListRepository.findByUserId(userId)
        .stream()
        .map(TaskListDTO::fromEntity)
        .collect(Collectors.toList());
  }

  public TaskListDTO getListById(UUID id, UUID userId) {
    TaskList taskList = taskListRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ResourceNotFoundException("TaskList", id));
    return TaskListDTO.fromEntity(taskList);
  }

  public TaskListDTO updateList(UUID id, UUID userId, String newTitle) {
    TaskList taskList = taskListRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ResourceNotFoundException("TaskList", id));
    taskList.setTitle(newTitle);
    taskList.setUpdatedAt(Instant.now());
    TaskList saved = taskListRepository.save(taskList);
    return TaskListDTO.fromEntity(saved);
  }

  public void deleteList(UUID id, UUID userId) {
    TaskList taskList = taskListRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> new ResourceNotFoundException("TaskList", id));
    int taskCount = taskRepository.countByListId(id);
    if (taskCount > 0) {
      throw new ConflictException("Cannot delete list with tasks. Delete all tasks first.");
    }
    taskListRepository.deleteById(id);
  }

}
