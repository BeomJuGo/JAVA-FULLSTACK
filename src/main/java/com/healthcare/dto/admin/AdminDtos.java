package com.healthcare.dto.admin;

import java.time.LocalDateTime;

public class AdminDtos {

    public static class HidePostRequest {
        public boolean hidden;
        public String reason;
    }

    public static class HideCommentRequest {
        public boolean hidden;
        public String reason;
    }

    public static class SuspendAccountRequest {
        public String reason;
    }

    public static class RestoreAccountRequest {
        public String reason;
    }

    public static class ForceEndMatchRequest {
        public String reason;
    }

    public static class LogView {
        public Long id;
        public Long adminAcc;
        public String actionType;
        public String targetType;
        public Long targetId;
        public String reason;
        public LocalDateTime createdAt;
        public LogView(Long id, Long adminAcc, String actionType, String targetType, Long targetId,
                       String reason, LocalDateTime createdAt) {
            this.id=id; this.adminAcc=adminAcc; this.actionType=actionType; this.targetType=targetType;
            this.targetId=targetId; this.reason=reason; this.createdAt=createdAt;
        }
    }

    public static class PageResponse<T> {
        public java.util.List<T> content;
        public int page; public int size; public long totalElements; public int totalPages; public boolean last;
        public PageResponse(java.util.List<T> content, int page, int size, long totalElements, int totalPages, boolean last){
            this.content=content; this.page=page; this.size=size; this.totalElements=totalElements; this.totalPages=totalPages; this.last=last;
        }
    }
}
