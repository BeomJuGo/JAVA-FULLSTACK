package com.healthcare.repository;

import com.healthcare.domain.PostLike;
import com.healthcare.domain.PostLike.PK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, PK> {
    boolean existsByPostIdAndAccountId(Long postId, Long accountId);
    long countByPostId(Long postId);
}
