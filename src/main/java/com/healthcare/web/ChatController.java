package com.healthcare.web;

import com.healthcare.domain.Account;
import com.healthcare.domain.Match;
import com.healthcare.domain.TrainerProfile;
import com.healthcare.dto.chat.ChatDtos;
import com.healthcare.repository.AccountRepository;
import com.healthcare.repository.MatchRepository;
import com.healthcare.repository.TrainerProfileRepository;
import com.healthcare.security.ActorGuard;
import com.healthcare.service.ChatService;
import com.healthcare.service.GptApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;
    private final com.healthcare.security.SecurityUtil securityUtil;
    private final GptApiService gptApiService;
    private final MatchRepository matchRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final AccountRepository accountRepository;
    private final ActorGuard actorGuard;

    public ChatController(ChatService service, 
                         com.healthcare.security.SecurityUtil securityUtil,
                         GptApiService gptApiService,
                         MatchRepository matchRepository,
                         TrainerProfileRepository trainerProfileRepository,
                         AccountRepository accountRepository,
                         ActorGuard actorGuard) {
        this.service = service;
        this.securityUtil = securityUtil;
        this.gptApiService = gptApiService;
        this.matchRepository = matchRepository;
        this.trainerProfileRepository = trainerProfileRepository;
        this.accountRepository = accountRepository;
        this.actorGuard = actorGuard;
    }

    // (1) 매칭별 스레드 생성/조회
    @PostMapping("/threads")
    public ResponseEntity<ChatDtos.ThreadResponse> createThread(@RequestBody ChatDtos.CreateThreadRequest req) {
        var t = service.createOrGetThread(req.matchId);
        return ResponseEntity.ok(new ChatDtos.ThreadResponse(t.getId(), t.getMatchId()));
    }

    // (2) 메시지 전송
    @PostMapping("/threads/{threadId}/messages")
    public ResponseEntity<Long> sendMessage(@PathVariable Long threadId,
                                            @RequestBody com.healthcare.dto.chat.ChatDtos.SendMessageRequest req) {
        Long senderAcc = securityUtil.currentAccountId();
        var m = service.sendMessage(threadId, senderAcc, req.content);
        return ResponseEntity.ok(m.getId());
    }


    // (3) 메시지 목록 조회 (페이지)
    @GetMapping("/threads/{threadId}/messages")
    public ResponseEntity<ChatDtos.PageResponse<ChatDtos.MessageView>> getMessages(
            @PathVariable Long threadId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "false") boolean includeHidden) {

        var pageObj = service.getMessages(threadId, page, size);
        var list = pageObj.getContent().stream()
                .filter(m -> includeHidden || !m.isHidden())
                .map(m -> new ChatDtos.MessageView(m.getId(), m.getSenderAcc(), m.getContent(), m.isHidden(), m.getCreatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ChatDtos.PageResponse<>(
                list, pageObj.getNumber(), pageObj.getSize(), pageObj.getTotalElements(), pageObj.getTotalPages(), pageObj.isLast()
        ));
    }

    // (4) 메시지 숨김/해제
    @PostMapping("/messages/{messageId}/hide")
    public ResponseEntity<Long> hideMessage(@PathVariable Long messageId, @RequestBody ChatDtos.HideRequest req) {
        var m = service.hideMessage(messageId, req.hidden);
        return ResponseEntity.ok(m.getId());
    }

    // (5) AI 채팅: GPT와 실시간 대화 (DB 저장 없음)
    @PostMapping("/ai/{matchId}/message")
    public ResponseEntity<ChatDtos.AiChatResponse> sendAiMessage(
            @PathVariable Long matchId,
            @RequestBody ChatDtos.AiChatRequest req) {
        
        // 매칭 접근 권한 확인
        actorGuard.requireAccessToMatch(matchId);
        
        // 매칭 정보 조회
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new com.healthcare.security.NotFoundException("매칭 정보를 찾을 수 없습니다."));
        
        // AI 트레이너인지 확인
        TrainerProfile trainer = trainerProfileRepository.findById(match.getTrainerId())
                .orElse(null);
        if (trainer == null) {
            throw new IllegalArgumentException("트레이너 정보를 찾을 수 없습니다.");
        }
        
        Account trainerAccount = accountRepository.findById(trainer.getAccountId())
                .orElse(null);
        if (trainerAccount == null || !"ai_trainer".equals(trainerAccount.getUsername())) {
            throw new IllegalArgumentException("이 매칭은 AI 트레이너와의 매칭이 아닙니다.");
        }
        
        // 대화 내역을 GPT API 형식으로 변환
        List<Map<String, String>> conversationHistory = new ArrayList<>();
        if (req.conversationHistory != null) {
            for (ChatDtos.ChatMessage msg : req.conversationHistory) {
                Map<String, String> map = new HashMap<>();
                map.put("role", msg.role);
                map.put("content", msg.content);
                conversationHistory.add(map);
            }
        }
        
        // GPT API 호출
        String gptResponse = gptApiService.chatWithGpt(conversationHistory, req.message);
        
        // 응답 반환
        ChatDtos.AiChatResponse response = new ChatDtos.AiChatResponse();
        response.message = gptResponse;
        return ResponseEntity.ok(response);
    }
}
