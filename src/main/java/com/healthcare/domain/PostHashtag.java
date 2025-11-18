package com.healthcare.domain;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name="post_hashtag")
@IdClass(PostHashtag.PK.class)
public class PostHashtag {

    @Id @Column(name="post_id")
    private Long postId;

    @Id @Column(name="hashtag_id")
    private Long hashtagId;

    public static class PK implements Serializable {
        public Long postId;
        public Long hashtagId;
        public PK() {}
        public PK(Long postId, Long hashtagId) { this.postId = postId; this.hashtagId = hashtagId; }
        @Override public int hashCode(){ return (postId+"-"+hashtagId).hashCode(); }
        @Override public boolean equals(Object o){
            if(this==o) return true; if(!(o instanceof PK p)) return false;
            return postId.equals(p.postId) && hashtagId.equals(p.hashtagId);
        }
    }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getHashtagId() { return hashtagId; }
    public void setHashtagId(Long hashtagId) { this.hashtagId = hashtagId; }
}
