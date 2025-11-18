package com.healthcare.web;

import com.healthcare.dto.ai.AiDtos;
import com.healthcare.service.AiRecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * AI 추천 컨트롤러
 */
@RestController
@RequestMapping("/api/ai")
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    public AiRecommendationController(AiRecommendationService aiRecommendationService) {
        this.aiRecommendationService = aiRecommendationService;
    }

    /**
     * AI 추천 플랜 생성 요청
     * 사용자 정보를 기반으로 GPT가 운동 및 식단 플랜을 생성하고 매칭을 설정합니다.
     * 
     * @param req 추천 요청 정보 (weekStart, goal, specialRequests)
     * @return 생성된 매칭 ID
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/recommendations")
    public ResponseEntity<AiDtos.RecommendationResponse> createRecommendation(
            @Valid @RequestBody AiDtos.RecommendationRequest req) {
        try {
            Long matchId = aiRecommendationService.createAiRecommendation(
                    req.weekStart,
                    req.goal,
                    req.specialRequests
            );
            
            return ResponseEntity.ok(new AiDtos.RecommendationResponse(
                    matchId,
                    "AI 추천 플랜이 성공적으로 생성되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new AiDtos.RecommendationResponse(
                            null,
                            "AI 추천 플랜 생성 중 오류가 발생했습니다: " + e.getMessage()
                    ));
        }
    }
}

