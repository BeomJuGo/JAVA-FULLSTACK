// src/main/java/com/healthcare/repository/MatchRepository.java
package com.healthcare.repository;

import com.healthcare.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    default Optional<Long> findUserProfileIdByMatchId(Long matchId) {
        return findById(matchId).map(Match::getUserProfileId);
    }

    default Optional<Long> findTrainerProfileIdByMatchId(Long matchId) {
        return findById(matchId).map(Match::getTrainerProfileId);
    }

    // 사용자 ID로 매칭 목록 조회
    List<Match> findByUserId(Long userId);

    // 트레이너 ID로 매칭 목록 조회
    List<Match> findByTrainerId(Long trainerId);

    // 사용자 ID와 상태로 매칭 목록 조회
    List<Match> findByUserIdAndStatus(Long userId, Match.Status status);

    // 트레이너 ID와 상태로 매칭 목록 조회
    List<Match> findByTrainerIdAndStatus(Long trainerId, Match.Status status);
}
