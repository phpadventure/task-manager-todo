package com.taskmanager.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskPriority;
import com.taskmanager.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
  int countByListId(UUID listId);

  List<Task> findByUserId(UUID userId);

  List<Task> findByListIdAndUserId(UUID listId, UUID userId);

  Optional<Task> findByIdAndUserId(UUID id, UUID userId);

  List<Task> findByUserIdAndStatus(UUID userId, TaskStatus status);

  List<Task> findByUserIdAndPriority(UUID userId, TaskPriority priority);

  @Query(
      "SELECT t FROM Task t WHERE t.userId = :userId AND t.status != 'DONE' "
          + "AND t.dueDate < CURRENT_DATE")
  List<Task> findOverdueTasks(@Param("userId") UUID userId);

  @Query(
      "SELECT t FROM Task t WHERE t.userId = :userId AND t.status != 'DONE' "
          + "AND t.dueDate >= CURRENT_DATE AND t.dueDate <= :dueSoonDate")
  List<Task> findDueSoonTasks(
      @Param("userId") UUID userId, @Param("dueSoonDate") LocalDate dueSoonDate);

  @Query(
      "SELECT t FROM Task t WHERE t.userId = :userId AND ("
          + "LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR "
          + "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')))")
  List<Task> searchByTitleOrDescription(
      @Param("userId") UUID userId, @Param("query") String query);
}
