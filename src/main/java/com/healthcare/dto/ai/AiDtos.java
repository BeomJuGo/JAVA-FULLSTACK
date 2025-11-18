package com.healthcare.dto.ai;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * AI 추천 관련 DTO
 */
public class AiDtos {

    /**
     * AI 추천 요청
     */
    public static class RecommendationRequest {
        @NotNull(message = "weekStart는 필수입니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate weekStart;

        /**
         * 목표 (예: "체중 감량", "근육 증가", "체력 향상" 등)
         */
        public String goal;

        /**
         * 특별 요청사항 (예: "무릎 부상으로 인해 하체 운동 제한", "채식 위주 식단" 등)
         */
        public String specialRequests;
    }

    /**
     * AI 추천 응답
     */
    public static class RecommendationResponse {
        public Long matchId;
        public String message;

        public RecommendationResponse(Long matchId, String message) {
            this.matchId = matchId;
            this.message = message;
        }
    }
}

