package com.healthcare.dto.match;

import java.time.LocalDateTime;

public class MatchDtos {

    public static class RequestCreate {
        public Long userId;      // user_profile.id
        public Long trainerId;   // trainer_profile.id
        public String requestedBy; // "USER" | "TRAINER"
    }

    public static class ReasonRequest {
        public String reason;
    }

    public static class SimpleResponse {
        public Long id;
        public String status;
        public SimpleResponse(Long id, String status) {
            this.id = id; this.status = status;
        }
    }

    /** 매칭 정보와 트레이너/사용자 정보를 포함한 응답 */
    public static class MatchWithPartnerInfo {
        public Long id;
        public Long userId;
        public Long trainerId;
        public String status;
        public String requestedBy;
        public LocalDateTime requestedAt;
        public LocalDateTime acceptedAt;
        public LocalDateTime endedAt;
        public String endReason;
        public boolean blocked;
        public String blockReason;
        public boolean reported;
        public String reportReason;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        
        // 상대방 정보 (사용자인 경우 트레이너 정보, 트레이너인 경우 사용자 정보)
        public String partnerName;      // displayName 또는 username
        public String partnerUsername;  // username
        public boolean isAiTrainer;     // AI 트레이너 여부
        
        public MatchWithPartnerInfo() {}
    }
}
