package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
public class Account {

    public enum Role { USER, TRAINER, ADMIN }
    public enum Status { ACTIVE, SUSPENDED, WITHDRAWN }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=100, unique=true)
    private String username;

    @Column(nullable=false, length=255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Status status = Status.ACTIVE;

    @Column(name="display_name", nullable=false, length=100)
    private String displayName;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate public void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    // getters/setters
    public Long getId(){ return id; }
    public String getUsername(){ return username; }
    public void setUsername(String username){ this.username = username; }
    public String getPassword(){ return password; }
    public void setPassword(String password){ this.password = password; }
    public Role getRole(){ return role; }
    public void setRole(Role role){ this.role = role; }
    public Status getStatus(){ return status; }
    public void setStatus(Status status){ this.status = status; }
    public String getDisplayName(){ return displayName; }
    public void setDisplayName(String displayName){ this.displayName = displayName; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
}
