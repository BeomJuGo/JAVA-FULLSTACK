package com.healthcare.dto.chat;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDtos {

    // 스레드
    public static class CreateThreadRequest {
        public Long matchId;
    }
    public static class ThreadResponse {
        public Long threadId;
        public Long matchId;
        public ThreadResponse(Long threadId, Long matchId) { this.threadId = threadId; this.matchId = matchId; }
    }

    // 메시지
    public static class SendMessageRequest {
        public Long senderAcc; // account.id (JWT 도입 전 임시)
        public String content;
    }

    public static class MessageView {
        public Long id;
        public Long senderAcc;
        public String content;
        public boolean hidden;
        public LocalDateTime createdAt;
        public MessageView(Long id, Long senderAcc, String content, boolean hidden, LocalDateTime createdAt) {
            this.id = id; this.senderAcc = senderAcc; this.content = content; this.hidden = hidden; this.createdAt = createdAt;
        }
    }

    public static class PageResponse<T> {
        public List<T> content;
        public int page;
        public int size;
        public long totalElements;
        public int totalPages;
        public boolean last;
        public PageResponse(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
            this.content = content; this.page = page; this.size = size;
            this.totalElements = totalElements; this.totalPages = totalPages; this.last = last;
        }
    }

    public static class HideRequest {
        public boolean hidden;
    }

    // AI 채팅
    public static class ChatMessage {
        public String role; // "user" or "assistant"
        public String content;
        
        public ChatMessage() {}
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class AiChatRequest {
        public String message;
        public List<ChatMessage> conversationHistory;
    }

    public static class AiChatResponse {
        public String message;
    }
}
