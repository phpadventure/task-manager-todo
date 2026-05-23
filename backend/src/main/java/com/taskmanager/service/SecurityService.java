package com.taskmanager.service;

import java.util.UUID;

import com.taskmanager.repository.TaskListRepository;
import com.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    private final TaskRepository taskRepository;
    private final TaskListRepository taskListRepository;
    private final SecurityContextService securityContextService;

    public SecurityService(TaskRepository taskRepository, TaskListRepository taskListRepository,
                          SecurityContextService securityContextService) {
        this.taskRepository = taskRepository;
        this.taskListRepository = taskListRepository;
        this.securityContextService = securityContextService;
    }

    public boolean userOwnsTask(UUID taskId) {
        try {
            UUID userId = securityContextService.getCurrentUserId();
            return taskRepository.findByIdAndUserId(taskId, userId).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean userOwnsTaskList(UUID listId) {
        try {
            UUID userId = securityContextService.getCurrentUserId();
            return taskListRepository.findByIdAndUserId(listId, userId).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean userOwnsTaskInList(UUID taskId, UUID listId) {
        try {
            UUID userId = securityContextService.getCurrentUserId();
            var task = taskRepository.findByIdAndUserId(taskId, userId);
            if (task.isEmpty()) {
                return false;
            }
            return task.get().getListId().equals(listId);
        } catch (Exception e) {
            return false;
        }
    }
}
