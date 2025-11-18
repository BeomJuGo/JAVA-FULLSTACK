package com.healthcare.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="match_id", nullable=false, unique=true)
    private Long matchId;

    @Column(name="user_id", nullable=false)
    private Long userId; // user_profile.id

    @Column(name="trainer_id", nullable=false)
    private Long trainerId; // trainer_profile.id

    @Column(name="rating", nullable=false, precision=2, scale=1)
    private BigDecimal rating; // 1.0 ~ 5.0

    @Column(name="content", nullable=false, length=2000)
    private String content;

    @Column(name="is_anonymous", nullable=false)
    private boolean anonymous = false;

    @Column(name="editable_until")
    private LocalDateTime editableUntil;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate public void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    // getters/setters
    public Long getId(){ return id; }
    public Long getMatchId(){ return matchId; }
    public void setMatchId(Long matchId){ this.matchId = matchId; }
    public Long getUserId(){ return userId; }
    public void setUserId(Long userId){ this.userId = userId; }
    public Long getTrainerId(){ return trainerId; }
    public void setTrainerId(Long trainerId){ this.trainerId = trainerId; }
    public BigDecimal getRating(){ return rating; }
    public void setRating(BigDecimal rating){ this.rating = rating; }
    public String getContent(){ return content; }
    public void setContent(String content){ this.content = content; }
    public boolean isAnonymous(){ return anonymous; }
    public void setAnonymous(boolean anonymous){ this.anonymous = anonymous; }
    public LocalDateTime getEditableUntil(){ return editableUntil; }
    public void setEditableUntil(LocalDateTime editableUntil){ this.editableUntil = editableUntil; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
}
