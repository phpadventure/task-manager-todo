package com.taskmanager.repository;

import java.util.UUID;

import com.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
  int countByListId(UUID listId);
}
