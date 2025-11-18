package com.healthcare.web;

import com.healthcare.domain.Review;
import com.healthcare.dto.review.ReviewDtos;
import com.healthcare.repository.UserProfileRepository;
import com.healthcare.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService service;
    private final com.healthcare.security.SecurityUtil securityUtil;
    private final com.healthcare.repository.UserProfileRepository userProfileRepo;

    public ReviewController(ReviewService service,
                           com.healthcare.security.SecurityUtil securityUtil,
                           com.healthcare.repository.UserProfileRepository userProfileRepo) {
        this.service = service;
        this.securityUtil = securityUtil;
        this.userProfileRepo = userProfileRepo;
    }

    // 생성
    @PostMapping
    public ResponseEntity<Long> create(@RequestBody ReviewDtos.CreateRequest req) {
        // accountId에서 userId로 변환
        Long accountId = securityUtil.currentAccountId();
        var userProfile = userProfileRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        req.userId = userProfile.getId();
        var r = service.create(req);
        return ResponseEntity.ok(r.getId());
    }

    // 수정 (작성자 userId를 accountId에서 변환)
    @PatchMapping("/{id}")
    public ResponseEntity<Long> update(@PathVariable Long id,
                                       @RequestBody com.healthcare.dto.review.ReviewDtos.UpdateRequest req) {
        Long accountId = securityUtil.currentAccountId();
        var userProfile = userProfileRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        Long userId = userProfile.getId();
        var r = service.update(id, userId, req);
        return ResponseEntity.ok(r.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long accountId = securityUtil.currentAccountId();
        var userProfile = userProfileRepo.findByAccountId(accountId)
                .orElseThrow(() -> new IllegalStateException("User profile not found"));
        Long userId = userProfile.getId();
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }


    // 매칭별 단건 조회
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<ReviewDtos.ReviewView> getByMatch(@PathVariable Long matchId) {
        var r = service.getByMatch(matchId);
        return ResponseEntity.ok(toView(r));
    }

    // 트레이너별 목록 + 요약(평점/개수)
    @GetMapping("/trainers/{trainerId}")
    public ResponseEntity<ReviewDtos.PageResponse<ReviewDtos.ReviewView>> pageByTrainer(
            @PathVariable Long trainerId,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size) {

        var pg = service.pageByTrainer(trainerId, page, size);
        var list = pg.getContent().stream().map(this::toView).collect(Collectors.toList());
        var summary = new ReviewDtos.PageSummary(service.avgRating(trainerId), service.countByTrainer(trainerId));
        return ResponseEntity.ok(new ReviewDtos.PageResponse<>(
                list, pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages(), pg.isLast(), summary
        ));
    }

    private ReviewDtos.ReviewView toView(Review r) {
        Long author = r.isAnonymous() ? null : r.getUserId();
        return new ReviewDtos.ReviewView(
                r.getId(), r.getTrainerId(),
                r.getRating().doubleValue(), r.getContent(),
                r.isAnonymous(), author, r.getCreatedAt(), r.getEditableUntil()
        );
    }
}
