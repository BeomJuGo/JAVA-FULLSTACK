package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_action_log", indexes = {
        @Index(name="idx_admin_action_target", columnList = "target_type, target_id")
})
public class AdminActionLog {

    public enum ActionType {
        HIDE_POST, UNHIDE_POST, HIDE_COMMENT, UNHIDE_COMMENT,
        SUSPEND_ACCOUNT, RESTORE_ACCOUNT, FORCE_END_MATCH
    }
    public enum TargetType { ACCOUNT, POST, COMMENT, MATCH }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="admin_acc", nullable=false)
    private Long adminAcc;

    @Enumerated(EnumType.STRING)
    @Column(name="action_type", nullable=false, length=30)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name="target_type", nullable=false, length=20)
    private TargetType targetType;

    @Column(name="target_id", nullable=false)
    private Long targetId;

    @Column(name="reason", length=500)
    private String reason;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters
    public Long getId() { return id; }
    public Long getAdminAcc() { return adminAcc; }
    public void setAdminAcc(Long adminAcc) { this.adminAcc = adminAcc; }
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
