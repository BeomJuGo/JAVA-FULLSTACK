package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="post_media", indexes = {
        @Index(name="idx_post_media_post", columnList = "post_id")
})
public class PostMedia {

    public enum MediaType { IMAGE, VIDEO }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="post_id", nullable = false)
    private Long postId;

    @Enumerated(EnumType.STRING)
    @Column(name="media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Column(length=500)
    private String url;

    @Column(name="public_id", nullable = false, length=255)
    private String publicId;

    private Integer width;
    private Integer height;

    @Column
    private Long bytes;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters/setters
    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Long getBytes() { return bytes; }
    public void setBytes(Long bytes) { this.bytes = bytes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
