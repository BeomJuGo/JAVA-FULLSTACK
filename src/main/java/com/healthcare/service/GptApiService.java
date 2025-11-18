package com.healthcare.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI GPT API를 호출하여 운동 및 식단 플랜을 생성하는 서비스
 */
@Service
public class GptApiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;
    private final ObjectMapper objectMapper;

    public GptApiService(@Value("${openai.api-key}") String apiKey,
            @Value("${openai.api-url}") String apiUrl,
            @Value("${openai.model}") String model) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.objectMapper = new ObjectMapper();

        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    /**
     * 사용자 정보를 기반으로 GPT에게 주간 운동 및 식단 플랜을 요청
     * 
     * @param userInfo  사용자 정보 (키: gender, age, heightCm, weightKg, activityLevel,
     *                  goal 등)
     * @param weekStart 주간 시작일
     * @return GPT가 생성한 플랜 데이터 (JSON 문자열)
     */
    public String generatePlan(Map<String, Object> userInfo, String weekStart) {
        String prompt = buildPrompt(userInfo, weekStart);

        ChatRequest request = new ChatRequest();
        request.model = this.model;
        request.messages = List.of(
                new ChatMessage("system",
                        "당신은 전문 피트니스 트레이너이자 영양사입니다. 사용자의 개인 정보를 바탕으로 주간 운동 계획과 식단을 구체적이고 실현 가능하게 추천해주세요. 응답은 반드시 JSON 형식으로 제공해야 합니다."),
                new ChatMessage("user", prompt));
        request.temperature = 0.7;
        request.maxTokens = 4000;

        try {
            // 요청 본문을 JSON으로 직렬화하여 로깅 (디버깅용)
            String requestJson = objectMapper.writeValueAsString(request);
            System.out.println("[GPT API] Request JSON: " + requestJson);
            System.out.println("[GPT API] Model: " + this.model);
            System.out.println("[GPT API] URL: " + this.apiUrl);

            ChatResponse response = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .defaultIfEmpty("(응답 본문 없음)")
                                        .flatMap(errorBody -> {
                                            System.err
                                                    .println("[GPT API] Error Status: " + clientResponse.statusCode());
                                            System.err.println("[GPT API] Error Response Body: " + errorBody);
                                            return Mono.error(new RuntimeException(
                                                    "OpenAI API 오류 (" + clientResponse.statusCode() + "): "
                                                            + errorBody));
                                        });
                            })
                    .bodyToMono(ChatResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response == null || response.choices == null || response.choices.isEmpty()) {
                throw new RuntimeException("GPT API 응답이 비어있습니다.");
            }

            return response.choices.get(0).message.content;
        } catch (Exception e) {
            System.err.println("[GPT API] Exception: " + e.getClass().getName() + " - " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[GPT API] Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("GPT API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 채팅 메시지를 GPT에게 전송하고 응답을 받음 (대화 컨텍스트 유지)
     * 
     * @param conversationHistory 이전 대화 내역 (user와 assistant 메시지 교차)
     * @param userMessage         사용자가 보낸 메시지
     * @return GPT 응답 메시지
     */
    public String chatWithGpt(List<Map<String, String>> conversationHistory, String userMessage) {
        ChatRequest request = new ChatRequest();
        request.model = this.model;

        // 시스템 메시지와 대화 내역 구성
        List<ChatMessage> messages = new java.util.ArrayList<>();
        messages.add(new ChatMessage("system",
                "당신은 전문 피트니스 트레이너이자 영양사입니다. 사용자의 운동, 식단, 건강 관련 질문에 친절하고 전문적으로 답변해주세요. " +
                        "구체적이고 실용적인 조언을 제공하며, 사용자의 목표와 상황을 고려한 맞춤형 답변을 해주세요."));

        // 이전 대화 내역 추가
        if (conversationHistory != null) {
            for (Map<String, String> msg : conversationHistory) {
                String role = msg.get("role");
                String content = msg.get("content");
                if (role != null && content != null && (role.equals("user") || role.equals("assistant"))) {
                    messages.add(new ChatMessage(role, content));
                }
            }
        }

        // 현재 사용자 메시지 추가
        messages.add(new ChatMessage("user", userMessage));

        request.messages = messages;
        request.temperature = 0.7;
        request.maxTokens = 1000;

        try {
            ChatResponse response = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                return clientResponse.bodyToMono(String.class)
                                        .defaultIfEmpty("(응답 본문 없음)")
                                        .flatMap(errorBody -> {
                                            System.err
                                                    .println("[GPT Chat] Error Status: " + clientResponse.statusCode());
                                            System.err.println("[GPT Chat] Error Response Body: " + errorBody);
                                            return Mono.error(new RuntimeException(
                                                    "OpenAI API 오류 (" + clientResponse.statusCode() + "): "
                                                            + errorBody));
                                        });
                            })
                    .bodyToMono(ChatResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response == null || response.choices == null || response.choices.isEmpty()) {
                throw new RuntimeException("GPT API 응답이 비어있습니다.");
            }

            return response.choices.get(0).message.content;
        } catch (Exception e) {
            System.err.println("[GPT Chat] Exception: " + e.getClass().getName() + " - " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[GPT Chat] Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("GPT 채팅 API 호출 실패: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(Map<String, Object> userInfo, String weekStart) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("다음 사용자 정보를 바탕으로 ").append(weekStart).append("부터 시작하는 1주일간의 운동 및 식단 플랜을 작성해주세요.\n\n");
        prompt.append("사용자 정보:\n");

        if (userInfo.containsKey("gender")) {
            prompt.append("- 성별: ").append(userInfo.get("gender")).append("\n");
        }
        if (userInfo.containsKey("age")) {
            prompt.append("- 나이: ").append(userInfo.get("age")).append("세\n");
        }
        if (userInfo.containsKey("heightCm")) {
            prompt.append("- 키: ").append(userInfo.get("heightCm")).append("cm\n");
        }
        if (userInfo.containsKey("weightKg")) {
            prompt.append("- 체중: ").append(userInfo.get("weightKg")).append("kg\n");
        }
        if (userInfo.containsKey("activityLevel")) {
            prompt.append("- 활동 수준: ").append(userInfo.get("activityLevel")).append("\n");
        }
        if (userInfo.containsKey("goal")) {
            prompt.append("- 목표: ").append(userInfo.get("goal")).append("\n");
        }
        if (userInfo.containsKey("specialRequests")) {
            prompt.append("- 특별 요청사항: ").append(userInfo.get("specialRequests")).append("\n");
        }

        prompt.append("\n다음 JSON 형식으로 응답해주세요:\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"주간 플랜 제목\",\n");
        prompt.append("  \"note\": \"전체 플랜에 대한 설명\",\n");
        prompt.append("  \"days\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"dayIndex\": 0,\n");
        prompt.append("      \"note\": \"해당 날짜에 대한 메모 (선택사항)\",\n");
        prompt.append("      \"items\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"itemType\": \"WORKOUT\",\n");
        prompt.append("          \"title\": \"운동 제목\",\n");
        prompt.append("          \"description\": \"운동 설명 및 방법\",\n");
        prompt.append("          \"targetMin\": 30\n");
        prompt.append("        },\n");
        prompt.append("        {\n");
        prompt.append("          \"itemType\": \"DIET\",\n");
        prompt.append("          \"title\": \"식단 제목\",\n");
        prompt.append("          \"description\": \"식단 설명 및 메뉴\",\n");
        prompt.append("          \"targetKcal\": 2000\n");
        prompt.append("        }\n");
        prompt.append("      ]\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("\n주의사항:\n");
        prompt.append("- dayIndex는 0(월요일)부터 6(일요일)까지입니다.\n");
        prompt.append("- itemType은 WORKOUT, DIET, NOTE 중 하나입니다.\n");
        prompt.append("- WORKOUT의 경우 targetMin(분)을, DIET의 경우 targetKcal(칼로리)를 제공하세요.\n");
        prompt.append("- 각 날짜마다 적절한 운동과 식단을 추천해주세요.\n");
        prompt.append("- 사용자의 활동 수준과 목표에 맞게 조절해주세요.\n");

        return prompt.toString();
    }

    // OpenAI API 요청/응답 DTO
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatRequest {
        public String model;
        public List<ChatMessage> messages;
        public Double temperature;

        @JsonProperty("max_tokens")
        public Integer maxTokens;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatMessage {
        public String role;
        public String content;

        public ChatMessage() {
        }

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatResponse {
        public List<Choice> choices;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        public ChatMessage message;
    }
}
