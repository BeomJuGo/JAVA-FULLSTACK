package com.healthcare.repository;

import com.healthcare.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // 엔티티에 Long accountId 필드가 있는 경우
    Optional<UserProfile> findByAccountId(Long accountId);
}
