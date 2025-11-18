package com.healthcare.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.domain.*;
import com.healthcare.dto.plan.PlanDtos;
import com.healthcare.repository.*;
import com.healthcare.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 추천 서비스: 사용자 정보를 기반으로 GPT를 통해 플랜을 생성하고 매칭을 설정
 */
@Service
public class AiRecommendationService {

    private final GptApiService gptApiService;
    private final AiTrainerInitService aiTrainerInitService;
    private final PlanService planService;
    private final UserProfileRepository userProfileRepository;
    private final MatchRepository matchRepository;
    private final PlanWeekRepository planWeekRepository;
    private final PlanDayRepository planDayRepository;
    private final PlanItemRepository planItemRepository;
    private final SecurityUtil securityUtil;
    private final ObjectMapper objectMapper;

    public AiRecommendationService(GptApiService gptApiService,
                                  AiTrainerInitService aiTrainerInitService,
                                  PlanService planService,
                                  UserProfileRepository userProfileRepository,
                                  MatchRepository matchRepository,
                                  PlanWeekRepository planWeekRepository,
                                  PlanDayRepository planDayRepository,
                                  PlanItemRepository planItemRepository,
                                  SecurityUtil securityUtil) {
        this.gptApiService = gptApiService;
        this.aiTrainerInitService = aiTrainerInitService;
        this.planService = planService;
        this.userProfileRepository = userProfileRepository;
        this.matchRepository = matchRepository;
        this.planWeekRepository = planWeekRepository;
        this.planDayRepository = planDayRepository;
        this.planItemRepository = planItemRepository;
        this.securityUtil = securityUtil;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * AI 추천 플랜 생성 및 매칭 설정
     * 
     * @param weekStart 주간 시작일
     * @param goal 목표 (선택사항)
     * @param specialRequests 특별 요청사항 (선택사항)
     * @return 생성된 매칭 ID
     */
    @Transactional
    public Long createAiRecommendation(LocalDate weekStart, String goal, String specialRequests) {
        // 현재 로그인한 사용자의 계정 ID
        Long accountId = securityUtil.currentAccountId();
        
        // 사용자 프로필 조회
        UserProfile userProfile = userProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("사용자 프로필을 찾을 수 없습니다."));

        // AI 트레이너 프로필 ID 조회
        Long aiTrainerProfileId = aiTrainerInitService.getAiTrainerProfileId();

        // 기존 매칭 확인 (AI 트레이너와의 진행 중인 매칭)
        var existingMatches = matchRepository.findByUserIdAndStatus(
                userProfile.getId(), Match.Status.IN_PROGRESS);
        
        Match match = existingMatches.stream()
                .filter(m -> m.getTrainerId().equals(aiTrainerProfileId))
                .findFirst()
                .orElseGet(() -> {
                    // 새 매칭 생성
                    var newMatch = new Match();
                    newMatch.setUserId(userProfile.getId());
                    newMatch.setTrainerId(aiTrainerProfileId);
                    newMatch.setRequestedBy(Match.RequestedBy.USER);
                    newMatch.setStatus(Match.Status.IN_PROGRESS); // AI 매칭은 자동으로 진행 상태
                    newMatch.setRequestedAt(LocalDateTime.now());
                    newMatch.setAcceptedAt(LocalDateTime.now()); // 자동 수락
                    return matchRepository.save(newMatch);
                });

        // 사용자 정보를 Map으로 구성
        Map<String, Object> userInfo = new HashMap<>();
        if (userProfile.getGender() != null) {
            userInfo.put("gender", userProfile.getGender().name());
        }
        if (userProfile.getAge() != null) {
            userInfo.put("age", userProfile.getAge());
        }
        if (userProfile.getHeightCm() != null) {
            userInfo.put("heightCm", userProfile.getHeightCm().doubleValue());
        }
        if (userProfile.getWeightKg() != null) {
            userInfo.put("weightKg", userProfile.getWeightKg().doubleValue());
        }
        if (userProfile.getActivityLevel() != null) {
            userInfo.put("activityLevel", userProfile.getActivityLevel().name());
        }
        if (goal != null && !goal.isBlank()) {
            userInfo.put("goal", goal);
        }
        if (specialRequests != null && !specialRequests.isBlank()) {
            userInfo.put("specialRequests", specialRequests);
        }

        // GPT API 호출하여 플랜 생성
        String gptResponse = gptApiService.generatePlan(userInfo, weekStart.toString());

        // GPT 응답을 파싱하여 플랜 생성
        try {
            JsonNode planJson = objectMapper.readTree(gptResponse);
            
            // JSON에서 플랜 정보 추출
            String title = planJson.has("title") ? planJson.get("title").asText() : "AI 추천 플랜";
            String note = planJson.has("note") ? planJson.get("note").asText() : null;
            
            // AI 트레이너 계정 ID
            Long aiTrainerAccountId = aiTrainerInitService.getAiTrainerAccountId();
            
            // 주간 플랜 생성 (권한 검사 없이 직접 생성)
            var existingWeek = planWeekRepository.findByMatchIdAndWeekStart(match.getId(), weekStart);
            PlanWeek week;
            if (existingWeek.isPresent()) {
                week = existingWeek.get();
            } else {
                week = new PlanWeek();
                week.setMatchId(match.getId());
                week.setWeekStart(weekStart);
                week.setTitle(title);
                week.setNote(note != null && !note.isBlank() ? note : null);
                week.setCreatedBy(aiTrainerAccountId);
                week = planWeekRepository.save(week);
                
                // Day 0~6 자동 생성
                for (int i = 0; i < 7; i++) {
                    var day = new PlanDay();
                    day.setWeekId(week.getId());
                    day.setDayIndex(i);
                    day.setNote(null);
                    planDayRepository.save(day);
                }
            }
            
            // 주간 플랜 조회 (days 포함) - 사용자는 매칭에 접근 가능하므로 getWeekView 사용 가능
            PlanDtos.WeekView weekView = planService.getWeekView(match.getId(), weekStart);
            
            // 각 날짜별 아이템 생성
            if (planJson.has("days") && planJson.get("days").isArray()) {
                JsonNode days = planJson.get("days");
                
                for (JsonNode dayJson : days) {
                    int dayIndex = dayJson.has("dayIndex") ? dayJson.get("dayIndex").asInt() : -1;
                    if (dayIndex < 0 || dayIndex >= weekView.days.size()) {
                        continue; // 유효하지 않은 dayIndex는 건너뛰기
                    }
                    
                    PlanDtos.DayView dayView = weekView.days.get(dayIndex);
                    String dayNote = dayJson.has("note") ? dayJson.get("note").asText() : null;
                    
                    // Day 메모 업데이트 (권한 검사 없이 직접 업데이트)
                    if (dayNote != null && !dayNote.isBlank()) {
                        var day = planDayRepository.findById(dayView.id)
                                .orElseThrow(() -> new IllegalStateException("Day not found"));
                        day.setNote(dayNote);
                        planDayRepository.save(day);
                    }
                    
                    // 아이템 생성 (권한 검사 없이 직접 생성)
                    if (dayJson.has("items") && dayJson.get("items").isArray()) {
                        JsonNode items = dayJson.get("items");
                        
                        for (JsonNode itemJson : items) {
                            String itemTypeStr = itemJson.has("itemType") ? 
                                    itemJson.get("itemType").asText().toUpperCase() : "NOTE";
                            
                            if (!itemTypeStr.equals("WORKOUT") && !itemTypeStr.equals("DIET") && !itemTypeStr.equals("NOTE")) {
                                continue; // 유효하지 않은 타입은 건너뛰기
                            }
                            
                            PlanItem.ItemType itemType = PlanItem.ItemType.valueOf(itemTypeStr);
                            var item = new PlanItem();
                            item.setDayId(dayView.id);
                            item.setItemType(itemType);
                            item.setTitle(itemJson.has("title") ? 
                                    itemJson.get("title").asText() : "제목 없음");
                            item.setDescription(itemJson.has("description") ? 
                                    itemJson.get("description").asText() : "");
                            
                            switch (itemType) {
                                case DIET -> {
                                    if (itemJson.has("targetKcal")) {
                                        item.setTargetKcal(itemJson.get("targetKcal").asInt());
                                    }
                                    item.setTargetMin(null);
                                }
                                case WORKOUT -> {
                                    if (itemJson.has("targetMin")) {
                                        item.setTargetMin(itemJson.get("targetMin").asInt());
                                    }
                                    item.setTargetKcal(null);
                                }
                                case NOTE -> {
                                    item.setTargetKcal(null);
                                    item.setTargetMin(null);
                                }
                            }
                            
                            planItemRepository.save(item);
                        }
                    }
                }
            }
            
            return match.getId();
        } catch (Exception e) {
            throw new RuntimeException("GPT 응답 파싱 또는 플랜 생성 실패: " + e.getMessage(), e);
        }
    }
}

