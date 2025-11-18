package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "plan_item")
public class PlanItem {

    public enum ItemType { DIET, WORKOUT, NOTE }
    public enum StatusMark { O, D, X } // O/△/X (△는 D)

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="day_id", nullable = false)
    private Long dayId;

    @Enumerated(EnumType.STRING)
    @Column(name="item_type", nullable = false, length = 10)
    private ItemType itemType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name="target_kcal")
    private Integer targetKcal;

    @Column(name="target_min")
    private Integer targetMin;

    @Enumerated(EnumType.STRING)
    @Column(name="status_mark", nullable = false, length = 1)
    private StatusMark statusMark = StatusMark.X;

    @Column(name="completed_at")
    private LocalDateTime completedAt;

    @Column(name="locked", nullable = false)
    private boolean locked = false;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate public void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    // getters/setters
    public Long getId() { return id; }
    public Long getDayId() { return dayId; }
    public void setDayId(Long dayId) { this.dayId = dayId; }
    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getTargetKcal() { return targetKcal; }
    public void setTargetKcal(Integer targetKcal) { this.targetKcal = targetKcal; }
    public Integer getTargetMin() { return targetMin; }
    public void setTargetMin(Integer targetMin) { this.targetMin = targetMin; }
    public StatusMark getStatusMark() { return statusMark; }
    public void setStatusMark(StatusMark statusMark) { this.statusMark = statusMark; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
