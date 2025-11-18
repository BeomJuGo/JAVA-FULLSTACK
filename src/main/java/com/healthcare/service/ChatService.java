package com.healthcare.service;

import com.healthcare.domain.ChatMessage;
import com.healthcare.domain.ChatThread;
import com.healthcare.repository.ChatMessageRepository;
import com.healthcare.repository.ChatThreadRepository;
import com.healthcare.repository.MatchRepository;
import com.healthcare.security.ActorGuard;
import com.healthcare.security.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ChatThreadRepository threadRepo;
    private final ChatMessageRepository messageRepo;
    private final MatchRepository matchRepo;
    private final ActorGuard guard;

    public ChatService(ChatThreadRepository threadRepo,
                       ChatMessageRepository messageRepo,
                       MatchRepository matchRepo,
                       ActorGuard guard) {
        this.threadRepo = threadRepo;
        this.messageRepo = messageRepo;
        this.matchRepo = matchRepo;
        this.guard = guard;
    }

    /** 매칭당 1개 스레드 생성 (있으면 그대로 반환) */
    @Transactional
    public ChatThread createOrGetThread(Long matchId) {
        guard.requireAccessToMatch(matchId);
        matchRepo.findById(matchId)
                .orElseThrow(() -> new NotFoundException("매칭 정보를 찾을 수 없습니다."));

        return threadRepo.findByMatchId(matchId).orElseGet(() -> {
            var t = new ChatThread();
            t.setMatchId(matchId);
            try {
                return threadRepo.save(t);
            } catch (DataIntegrityViolationException ex) {
                // 동시 요청 등으로 중복 생성이 시도된 경우 기존 스레드 반환
                return threadRepo.findByMatchId(matchId)
                        .orElseThrow(() -> ex);
            }
        });
    }

    /** 메시지 전송 */
    @Transactional
    public ChatMessage sendMessage(Long threadId, Long senderAcc, String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content is blank");
        var thread = threadRepo.findById(threadId)
                .orElseThrow(() -> new NotFoundException("채팅 스레드를 찾을 수 없습니다."));
        guard.requireAccessToMatch(thread.getMatchId());

        var message = new ChatMessage();
        message.setThreadId(thread.getId());
        message.setSenderAcc(senderAcc);
        message.setContent(content);
        return messageRepo.save(message);
    }

    /** 메시지 숨김/해제 (모더레이션) */
    @Transactional
    public ChatMessage hideMessage(Long messageId, boolean hidden) {
        var message = messageRepo.findById(messageId)
                .orElseThrow(() -> new NotFoundException("채팅 메시지를 찾을 수 없습니다."));
        var thread = threadRepo.findById(message.getThreadId())
                .orElseThrow(() -> new NotFoundException("채팅 스레드를 찾을 수 없습니다."));
        guard.requireAccessToMatch(thread.getMatchId());

        message.setHidden(hidden);
        return message;
    }

    /** 메시지 페이지 조회 (오름차순 시간 정렬) */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<ChatMessage> getMessages(Long threadId, int page, int size) {
        var thread = threadRepo.findById(threadId)
                .orElseThrow(() -> new NotFoundException("채팅 스레드를 찾을 수 없습니다."));
        guard.requireAccessToMatch(thread.getMatchId());

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return messageRepo.findByThreadIdOrderByCreatedAtAsc(threadId, pageable);
    }
}
