package com.healthcare.repository;

import com.healthcare.domain.PostView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
    long countByPostId(Long postId);
    boolean existsByPostIdAndAccountIdAndViewDate(Long postId, Long accountId, LocalDate viewDate);
}
