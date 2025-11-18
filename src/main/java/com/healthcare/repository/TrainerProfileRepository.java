package com.healthcare.repository;

import com.healthcare.domain.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {

    // 엔티티에 Long accountId 필드가 있는 경우
    Optional<TrainerProfile> findByAccountId(Long accountId);
}
