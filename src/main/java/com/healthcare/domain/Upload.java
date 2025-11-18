package com.healthcare.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "upload")
public class Upload {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 업로더 계정 id (삭제/숨김 권한 검증용) */
    @Column(name="uploader_account_id", nullable = false)
    private Long uploaderAccountId;

    /** Cloudinary public_id (필수) */
    @Column(name="public_id", nullable = false, length = 255, unique = true)
    private String publicId;

    /** secure_url */
    @Column(name="secure_url", length = 1000)
    private String secureUrl;

    /** 원본 파일명(클라가 전달) */
    @Column(name="original_name", length = 255)
    private String originalName;

    /** MIME/리소스 타입(image/video/raw) */
    @Column(name="resource_type", length = 50)
    private String resourceType;

    /** 포맷(jpg, png 등) */
    @Column(name="format", length = 50)
    private String format;

    /** 바이트 크기 */
    @Column(name="bytes_size")
    private Long bytesSize;

    /** 폭/높이 (이미지/비디오면) */
    @Column(name="width")
    private Integer width;
    @Column(name="height")
    private Integer height;

    /** 폴더(조직화용), 태그/컨텍스트 등 */
    @Column(name="folder", length = 255)
    private String folder;
    @Column(name="context", length = 500)
    private String context;

    /** 숨김 처리 */
    @Column(name="hidden", nullable = false)
    private boolean hidden = false;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="deleted_at")
    private LocalDateTime deletedAt;

    // getters/setters
    public Long getId() { return id; }
    public Long getUploaderAccountId() { return uploaderAccountId; }
    public void setUploaderAccountId(Long uploaderAccountId) { this.uploaderAccountId = uploaderAccountId; }
    public String getPublicId() { return publicId; }
    public void setPublicId(String publicId) { this.publicId = publicId; }
    public String getSecureUrl() { return secureUrl; }
    public void setSecureUrl(String secureUrl) { this.secureUrl = secureUrl; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Long getBytesSize() { return bytesSize; }
    public void setBytesSize(Long bytesSize) { this.bytesSize = bytesSize; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public String getFolder() { return folder; }
    public void setFolder(String folder) { this.folder = folder; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
