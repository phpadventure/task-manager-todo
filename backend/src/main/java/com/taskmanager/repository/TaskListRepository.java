package com.taskmanager.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.taskmanager.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, UUID> {
  List<TaskList> findByUserId(UUID userId);

  Optional<TaskList> findByIdAndUserId(UUID id, UUID userId);
}
