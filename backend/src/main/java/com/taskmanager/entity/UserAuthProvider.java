package com.taskmanager.entity;

import java.sql.Types;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(
    name = "user_auth_provider",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "unique_provider_user",
          columnNames = {"provider", "provider_user_id"}),
      @UniqueConstraint(name = "unique_user_provider", columnNames = {"user_id", "provider"})
    })
public class UserAuthProvider {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "CHAR(36)")
  @JdbcTypeCode(Types.VARCHAR)
  private UUID id;

  @Column(name = "user_id", nullable = false, columnDefinition = "CHAR(36)")
  @JdbcTypeCode(Types.VARCHAR)
  private UUID userId;

  @ManyToOne
  @JoinColumn(
      name = "user_id",
      nullable = false,
      insertable = false,
      updatable = false,
      foreignKey = @ForeignKey(name = "fk_user_auth_provider_user_id"))
  private User user;

  @Column(nullable = false)
  private String provider;

  @Column(name = "provider_user_id", nullable = false)
  private String providerUserId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
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

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getProviderUserId() {
    return providerUserId;
  }

  public void setProviderUserId(String providerUserId) {
    this.providerUserId = providerUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
