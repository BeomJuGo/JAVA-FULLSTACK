package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`match`")
public class Match {

    public enum Status {
        REQUESTED, ACCEPTED, IN_PROGRESS, ENDED, REJECTED, FORCE_ENDED
    }

    public enum RequestedBy {
        USER, TRAINER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_profile.id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // trainer_profile.id
    @Column(name = "trainer_id", nullable = false)
    private Long trainerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.REQUESTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_by", nullable = false, length = 10)
    private RequestedBy requestedBy;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "end_reason", length = 500)
    private String endReason;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked = false;

    @Column(name = "block_reason", length = 500)
    private String blockReason;

    @Column(name = "is_reported", nullable = false)
    private boolean reported = false;

    @Column(name = "report_reason", length = 500)
    private String reportReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ✅ getters & setters
    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public RequestedBy getRequestedBy() { return requestedBy; }
    public void setRequestedBy(RequestedBy requestedBy) { this.requestedBy = requestedBy; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(LocalDateTime acceptedAt) { this.acceptedAt = acceptedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public String getEndReason() { return endReason; }
    public void setEndReason(String endReason) { this.endReason = endReason; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }

    public boolean isReported() { return reported; }
    public void setReported(boolean reported) { this.reported = reported; }

    public String getReportReason() { return reportReason; }
    public void setReportReason(String reportReason) { this.reportReason = reportReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ✅ ActorGuard 호환용 alias getter
    public Long getUserProfileId() { return this.userId; }
    public Long getTrainerProfileId() { return this.trainerId; }
}
