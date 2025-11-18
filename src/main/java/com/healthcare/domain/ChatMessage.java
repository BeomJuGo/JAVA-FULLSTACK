package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="thread_id", nullable = false)
    private Long threadId;

    @Column(name="sender_acc", nullable = false)
    private Long senderAcc; // account.id

    // ✅ @Lob 제거, TEXT로 고정
    @Column(name="content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="is_hidden", nullable = false)
    private boolean hidden = false;

    public Long getId() { return id; }
    public Long getThreadId() { return threadId; }
    public void setThreadId(Long threadId) { this.threadId = threadId; }
    public Long getSenderAcc() { return senderAcc; }
    public void setSenderAcc(Long senderAcc) { this.senderAcc = senderAcc; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
}
