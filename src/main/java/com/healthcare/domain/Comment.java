package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="comment", indexes = {
        @Index(name="idx_comment_post_created", columnList = "post_id, created_at")
})
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="post_id", nullable = false)
    private Long postId;

    @Column(name="author_acc", nullable = false)
    private Long authorAcc;

    @Column(name="content", nullable = false, length = 1000)
    private String content;

    @Column(name="is_hidden", nullable = false)
    private boolean hidden = false;

    @Column(name="hidden_reason", length=500)
    private String hiddenReason;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate public void onUpdate(){ this.updatedAt = LocalDateTime.now(); }

    public Long getId(){ return id; }
    public Long getPostId(){ return postId; }
    public void setPostId(Long postId){ this.postId = postId; }
    public Long getAuthorAcc(){ return authorAcc; }
    public void setAuthorAcc(Long authorAcc){ this.authorAcc = authorAcc; }
    public String getContent(){ return content; }
    public void setContent(String content){ this.content = content; }
    public boolean isHidden(){ return hidden; }
    public void setHidden(boolean hidden){ this.hidden = hidden; }
    public String getHiddenReason(){ return hiddenReason; }
    public void setHiddenReason(String hiddenReason){ this.hiddenReason = hiddenReason; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getUpdatedAt(){ return updatedAt; }
}
