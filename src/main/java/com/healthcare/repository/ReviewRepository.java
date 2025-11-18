package com.healthcare.repository;

import com.healthcare.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 매치 단건 리뷰 (매치당 1개 정책일 때) — Review에 matchId(Long) 가정 */
    Optional<Review> findByMatchId(Long matchId);

    /** 트레이너 리뷰 페이징 (최신순) — Review에 trainerId(Long), createdAt 존재 가정 */
    Page<Review> findByTrainerIdOrderByCreatedAtDesc(Long trainerId, Pageable pageable);

    /** 트레이너 평균 평점 — Review에 rating(Number) + trainerId(Long) 존재 가정 */
    @Query("select avg(r.rating) from Review r where r.trainerId = :trainerId")
    Double avgRatingByTrainer(Long trainerId);

    /** 트레이너 리뷰 개수 — 파생 메서드로 충분 */
    long countByTrainerId(Long trainerId);

    /** (권한 검증용) 리뷰의 작성 유저 id — Review에 userId(Long) 존재 가정 */
    @Query("select r.userId from Review r where r.id = :reviewId")
    Optional<Long> findUserProfileIdByReviewId(Long reviewId);
}
