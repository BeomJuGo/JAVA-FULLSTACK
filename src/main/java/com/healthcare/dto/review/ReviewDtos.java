package com.healthcare.dto.review;

import java.time.LocalDateTime;

public class ReviewDtos {

    public static class CreateRequest {
        public Long matchId;
        public Long userId;
        public Long trainerId;
        public Double rating;      // 1.0~5.0
        public String content;     // <=2000
        public Boolean anonymous;  // optional
    }

    public static class UpdateRequest {
        public Double rating;      // optional
        public String content;     // optional
        public Boolean anonymous;  // optional
    }

    public static class ReviewView {
        public Long id;
        public Long trainerId;
        public Double rating;
        public String content;
        public boolean anonymous;
        public Long authorUserId;          // 익명일 땐 null
        public LocalDateTime createdAt;
        public LocalDateTime editableUntil;

        public ReviewView(Long id, Long trainerId, Double rating, String content, boolean anonymous,
                          Long authorUserId, LocalDateTime createdAt, LocalDateTime editableUntil) {
            this.id=id; this.trainerId=trainerId; this.rating=rating; this.content=content;
            this.anonymous=anonymous; this.authorUserId=authorUserId; this.createdAt=createdAt; this.editableUntil=editableUntil;
        }
    }

    public static class PageSummary {
        public double avgRating;
        public long totalReviews;
        public PageSummary(double avgRating, long totalReviews) {
            this.avgRating = avgRating; this.totalReviews = totalReviews;
        }
    }

    public static class PageResponse<T> {
        public java.util.List<T> content;
        public int page; public int size; public long totalElements; public int totalPages; public boolean last;
        public PageSummary summary;
        public PageResponse(java.util.List<T> content, int page, int size, long totalElements, int totalPages, boolean last, PageSummary summary){
            this.content=content; this.page=page; this.size=size; this.totalElements=totalElements; this.totalPages=totalPages; this.last=last; this.summary=summary;
        }
    }
}
