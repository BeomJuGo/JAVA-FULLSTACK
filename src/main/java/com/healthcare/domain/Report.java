package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="report", indexes = {
        @Index(name="idx_report_target", columnList = "target_type, target_id")
})
public class Report {

    public enum TargetType { POST, COMMENT, CHAT_MESSAGE }
    public enum Status { PENDING, REVIEWING, RESOLVED, REJECTED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name="target_id", nullable = false)
    private Long targetId;

    @Column(name="reporter_acc", nullable = false)
    private Long reporterAcc;

    @Column(name="reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name="resolution", length = 500)
    private String resolution;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate public void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    // getters/setters
    public Long getId(){ return id; }
    public TargetType getTargetType(){ return targetType; }
    public void setTargetType(TargetType targetType){ this.targetType = targetType; }
    public Long getTargetId(){ return targetId; }
    public void setTargetId(Long targetId){ this.targetId = targetId; }
    public Long getReporterAcc(){ return reporterAcc; }
    public void setReporterAcc(Long reporterAcc){ this.reporterAcc = reporterAcc; }
    public String getReason(){ return reason; }
    public void setReason(String reason){ this.reason = reason; }
    public Status getStatus(){ return status; }
    public void setStatus(Status status){ this.status = status; }
    public String getResolution(){ return resolution; }
    public void setResolution(String resolution){ this.resolution = resolution; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
}
