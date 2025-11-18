package com.healthcare.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name="post_like")
@IdClass(PostLike.PK.class)
public class PostLike {

    @Id @Column(name="post_id")
    private Long postId;

    @Id @Column(name="account_id")
    private Long accountId;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public static class PK implements Serializable {
        public Long postId; public Long accountId;
        public PK() {}
        public PK(Long postId, Long accountId){ this.postId=postId; this.accountId=accountId; }
        @Override public int hashCode(){ return (postId+"-"+accountId).hashCode(); }
        @Override public boolean equals(Object o){
            if(this==o) return true; if(!(o instanceof PK p)) return false;
            return postId.equals(p.postId) && accountId.equals(p.accountId);
        }
    }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
