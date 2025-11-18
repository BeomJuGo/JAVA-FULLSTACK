package com.healthcare.repository;

import com.healthcare.domain.ChatThread;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatThreadRepository extends JpaRepository<ChatThread, Long> {
    Optional<ChatThread> findByMatchId(Long matchId);
}
