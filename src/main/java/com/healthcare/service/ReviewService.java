package com.healthcare.service;

import com.healthcare.domain.Match;
import com.healthcare.domain.Review;
import com.healthcare.dto.review.ReviewDtos;
import com.healthcare.repository.MatchRepository;
import com.healthcare.repository.ReviewRepository;
import com.healthcare.security.ForbiddenException;
import com.healthcare.security.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final MatchRepository matchRepo;

    public ReviewService(ReviewRepository reviewRepo, MatchRepository matchRepo) {
        this.reviewRepo = reviewRepo;
        this.matchRepo = matchRepo;
    }

    private void validateRating(double r) {
        if (r < 1.0 || r > 5.0) throw new IllegalArgumentException("rating must be between 1.0 and 5.0");
    }

    /** 생성: 매칭 유저만 작성 가능, matchId 유일 */
    @Transactional
    public Review create(ReviewDtos.CreateRequest req) {
        validateRating(req.rating);
        // match 존재 & 일치 확인
        var m = matchRepo.findById(req.matchId)
                .orElseThrow(() -> new NotFoundException("매칭 정보를 찾을 수 없습니다."));

        if (!m.getUserId().equals(req.userId)) {
            throw new ForbiddenException("해당 매칭의 사용자만 리뷰를 작성할 수 있습니다.");
        }
        if (m.getStatus() != Match.Status.ENDED && m.getStatus() != Match.Status.FORCE_ENDED) {
            throw new IllegalStateException("종료된 매칭에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        Long trainerId = m.getTrainerId();
        if (req.trainerId != null && !trainerId.equals(req.trainerId)) {
            throw new IllegalStateException("요청한 트레이너 정보가 매칭과 일치하지 않습니다.");
        }

        if (reviewRepo.findByMatchId(req.matchId).isPresent())
            throw new IllegalStateException("이미 해당 매칭에 대한 리뷰가 존재합니다.");

        var r = new Review();
        r.setMatchId(req.matchId);
        r.setUserId(req.userId);
        r.setTrainerId(trainerId);
        r.setRating(BigDecimal.valueOf(req.rating).setScale(1, java.math.RoundingMode.HALF_UP));
        r.setContent(req.content);
        r.setAnonymous(Boolean.TRUE.equals(req.anonymous));
        r.setEditableUntil(LocalDateTime.now().plusDays(30));
        return reviewRepo.save(r);
    }

    /** 수정: 작성자 + 30일 내 */
    @Transactional
    public Review update(Long reviewId, Long userId, ReviewDtos.UpdateRequest req) {
        var r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("리뷰를 찾을 수 없습니다."));
        if (!r.getUserId().equals(userId)) throw new IllegalStateException("only author can update");
        if (r.getEditableUntil() == null || LocalDateTime.now().isAfter(r.getEditableUntil()))
            throw new IllegalStateException("edit window expired");

        if (req.rating != null) {
            validateRating(req.rating);
            r.setRating(BigDecimal.valueOf(req.rating).setScale(1, java.math.RoundingMode.HALF_UP));
        }
        if (req.content != null) r.setContent(req.content);
        if (req.anonymous != null) r.setAnonymous(req.anonymous);
        return r;
    }

    /** 삭제: 작성자 + 30일 내 */
    @Transactional
    public void delete(Long reviewId, Long userId) {
        var r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("리뷰를 찾을 수 없습니다."));
        if (!r.getUserId().equals(userId)) throw new IllegalStateException("only author can delete");
        if (r.getEditableUntil() == null || LocalDateTime.now().isAfter(r.getEditableUntil()))
            throw new IllegalStateException("delete window expired");
        reviewRepo.delete(r);
    }

    /** 트레이너별 페이지 + 평균/개수 */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Review> pageByTrainer(Long trainerId, int page, int size) {
        return reviewRepo.findByTrainerIdOrderByCreatedAtDesc(trainerId, PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public double avgRating(Long trainerId) {
        Double avg = reviewRepo.avgRatingByTrainer(trainerId);
        return avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public long countByTrainer(Long trainerId) {
        return reviewRepo.countByTrainerId(trainerId);
    }

    @Transactional(readOnly = true)
    public Review getByMatch(Long matchId) {
        return reviewRepo.findByMatchId(matchId)
                .orElseThrow(() -> new NotFoundException("리뷰를 찾을 수 없습니다."));
    }
}
