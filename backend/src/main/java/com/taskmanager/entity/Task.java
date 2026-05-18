package com.taskmanager.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table(
    name = "task",
    indexes = {
      @Index(name = "idx_user_id", columnList = "user_id"),
      @Index(name = "idx_list_id", columnList = "list_id"),
      @Index(name = "idx_status", columnList = "status"),
      @Index(name = "idx_priority", columnList = "priority"),
      @Index(name = "idx_due_date", columnList = "due_date")
    })
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "CHAR(36)")
  private UUID id;

  @Column(name = "list_id", nullable = false, columnDefinition = "CHAR(36)")
  private UUID listId;

  @ManyToOne
  @JoinColumn(
      name = "list_id",
      nullable = false,
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_task_list_id"))
  private TaskList list;

  @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
  private UUID userId;

  @ManyToOne
  @JoinColumn(
      name = "user_id",
      nullable = false,
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_task_user_id"))
  private User user;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private String priority;

  @Column(name = "due_date")
  private LocalDate dueDate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getListId() {
    return listId;
  }

  public void setListId(UUID listId) {
    this.listId = listId;
  }

  public TaskList getList() {
    return list;
  }

  public void setList(TaskList list) {
    this.list = list;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(UUID userId) {
    this.userId = userId;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
