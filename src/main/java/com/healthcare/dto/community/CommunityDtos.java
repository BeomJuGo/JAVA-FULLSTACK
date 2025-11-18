package com.healthcare.dto.community;

import java.time.LocalDateTime;
import java.util.List;

public class CommunityDtos {

    // Post
    public static class PostCreateRequest {
        public Long authorAcc;
        public String title;
        public String content;
        public List<MediaCreate> mediaList;     // optional
        public List<String> hashtags;           // optional (["diet","legday"])
    }
    public static class PostUpdateRequest {
        public String title;
        public String content;
        public List<MediaCreate> mediaList;     // 전체 교체 정책(간단)
        public List<String> hashtags;
    }
    public static class MediaCreate {
        public String mediaType;   // IMAGE | VIDEO
        public String publicId;    // Cloudinary public_id (required)
        public String url;         // optional
        public Integer width;      // optional
        public Integer height;     // optional
        public Long bytes;         // optional
    }

    public static class PostView {
        public Long id;
        public Long authorAcc;
        public String authorUsername;
        public String authorDisplayName;
        public String authorRole;
        public Long authorProfileId;
        public String title;
        public String content;
        public boolean hidden;
        public long likeCount;
        public long viewCount;
        public List<MediaView> media;
        public List<String> hashtags;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public PostView(Long id,
                        Long authorAcc,
                        String authorUsername,
                        String authorDisplayName,
                        String authorRole,
                        Long authorProfileId,
                        String title,
                        String content,
                        boolean hidden,
                        long likeCount,
                        long viewCount,
                        List<MediaView> media,
                        List<String> hashtags,
                        LocalDateTime createdAt,
                        LocalDateTime updatedAt) {
            this.id = id;
            this.authorAcc = authorAcc;
            this.authorUsername = authorUsername;
            this.authorDisplayName = authorDisplayName;
            this.authorRole = authorRole;
            this.authorProfileId = authorProfileId;
            this.title = title;
            this.content = content;
            this.hidden = hidden;
            this.likeCount = likeCount;
            this.viewCount = viewCount;
            this.media = media;
            this.hashtags = hashtags;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
    public static class MediaView {
        public Long id; public String mediaType; public String publicId; public String url;
        public Integer width; public Integer height; public Long bytes;
        public MediaView(Long id, String mediaType, String publicId, String url, Integer width, Integer height, Long bytes){
            this.id=id; this.mediaType=mediaType; this.publicId=publicId; this.url=url; this.width=width; this.height=height; this.bytes=bytes;
        }
    }

    public static class PageResponse<T> {
        public List<T> content;
        public int page; public int size; public long totalElements; public int totalPages; public boolean last;
        public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last){
            this.content=content; this.page=page; this.size=size; this.totalElements=totalElements; this.totalPages=totalPages; this.last=last;
        }
    }

    // Comment
    public static class CommentCreateRequest {
        public Long authorAcc;
        public String content;
    }
    public static class CommentView {
        public Long id;
        public Long authorAcc;
        public String authorUsername;
        public String authorDisplayName;
        public String authorRole;
        public Long authorProfileId;
        public String content;
        public boolean hidden;
        public LocalDateTime createdAt;
        public CommentView(Long id,
                           Long authorAcc,
                           String authorUsername,
                           String authorDisplayName,
                           String authorRole,
                           Long authorProfileId,
                           String content,
                           boolean hidden,
                           LocalDateTime createdAt){
            this.id=id;
            this.authorAcc=authorAcc;
            this.authorUsername=authorUsername;
            this.authorDisplayName=authorDisplayName;
            this.authorRole=authorRole;
            this.authorProfileId=authorProfileId;
            this.content=content;
            this.hidden=hidden;
            this.createdAt=createdAt;
        }
    }

    // Hide/Report
    public static class HideRequest { public boolean hidden; public String reason; }

    public static class ReportCreateRequest {
        public String targetType; // POST | COMMENT | CHAT_MESSAGE
        public Long targetId;
        public Long reporterAcc;
        public String reason;
    }
}
