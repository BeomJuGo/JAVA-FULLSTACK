package com.healthcare.web;

import com.healthcare.domain.Match;
import com.healthcare.domain.Account;
import com.healthcare.domain.TrainerProfile;
import com.healthcare.domain.UserProfile;
import com.healthcare.dto.match.MatchDtos;
import com.healthcare.repository.AccountRepository;
import com.healthcare.repository.UserProfileRepository;
import com.healthcare.repository.TrainerProfileRepository;
import com.healthcare.security.SecurityUtil;
import com.healthcare.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService service;
    private final SecurityUtil securityUtil;
    private final UserProfileRepository userProfileRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final AccountRepository accountRepository;

    public MatchController(MatchService service, SecurityUtil securityUtil,
                          UserProfileRepository userProfileRepository,
                          TrainerProfileRepository trainerProfileRepository,
                          AccountRepository accountRepository) {
        this.service = service;
        this.securityUtil = securityUtil;
        this.userProfileRepository = userProfileRepository;
        this.trainerProfileRepository = trainerProfileRepository;
        this.accountRepository = accountRepository;
    }

    // 매칭 요청 생성
    @PostMapping("/request")
    public ResponseEntity<MatchDtos.SimpleResponse> request(@RequestBody MatchDtos.RequestCreate req) {
        var m = service.createRequest(req);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 승인(트레이너)
    @PostMapping("/{id}/accept")
    public ResponseEntity<MatchDtos.SimpleResponse> accept(@PathVariable Long id) {
        var m = service.accept(id);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 시작(IN_PROGRESS 전환)
    @PostMapping("/{id}/start")
    public ResponseEntity<MatchDtos.SimpleResponse> start(@PathVariable Long id) {
        var m = service.start(id);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 종료(사유)
    @PostMapping("/{id}/end")
    public ResponseEntity<MatchDtos.SimpleResponse> end(@PathVariable Long id,
                                                        @RequestBody MatchDtos.ReasonRequest req) {
        var m = service.end(id, req.reason);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 거절(사유)
    @PostMapping("/{id}/reject")
    public ResponseEntity<MatchDtos.SimpleResponse> reject(@PathVariable Long id,
                                                           @RequestBody MatchDtos.ReasonRequest req) {
        var m = service.reject(id, req.reason);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 강제 종료(사유)
    @PostMapping("/{id}/force-end")
    public ResponseEntity<MatchDtos.SimpleResponse> forceEnd(@PathVariable Long id,
                                                             @RequestBody MatchDtos.ReasonRequest req) {
        var m = service.forceEnd(id, req.reason);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 차단(사유)
    @PostMapping("/{id}/block")
    public ResponseEntity<MatchDtos.SimpleResponse> block(@PathVariable Long id,
                                                          @RequestBody MatchDtos.ReasonRequest req) {
        var m = service.block(id, req.reason);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 신고(사유)
    @PostMapping("/{id}/report")
    public ResponseEntity<MatchDtos.SimpleResponse> report(@PathVariable Long id,
                                                           @RequestBody MatchDtos.ReasonRequest req) {
        var m = service.report(id, req.reason);
        return ResponseEntity.ok(new MatchDtos.SimpleResponse(m.getId(), m.getStatus().name()));
    }

    // 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<Match> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    // 현재 사용자의 매칭 목록 조회
    @GetMapping
    public ResponseEntity<List<MatchDtos.MatchWithPartnerInfo>> getMyMatches(@RequestParam(required = false) Match.Status status) {
        Long accountId = securityUtil.currentAccountId();
        String role = securityUtil.currentRole();
        List<Match> matches;

        if ("USER".equals(role)) {
            // 사용자인 경우: user_profile에서 id 조회 후 매칭 조회
            var userProfile = userProfileRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalStateException("User profile not found"));
            if (status != null) {
                matches = service.findByUserIdAndStatus(userProfile.getId(), status);
            } else {
                matches = service.findByUserId(userProfile.getId());
            }
        } else if ("TRAINER".equals(role)) {
            // 트레이너인 경우: trainer_profile에서 id 조회 후 매칭 조회
            var trainerProfile = trainerProfileRepository.findByAccountId(accountId)
                    .orElseThrow(() -> new IllegalStateException("Trainer profile not found"));
            if (status != null) {
                matches = service.findByTrainerIdAndStatus(trainerProfile.getId(), status);
            } else {
                matches = service.findByTrainerId(trainerProfile.getId());
            }
        } else {
            throw new IllegalStateException("Invalid role for match listing");
        }

        // 매칭 정보에 상대방 정보 추가 (종료된 매칭 제외)
        List<MatchDtos.MatchWithPartnerInfo> result = new ArrayList<>();
        for (Match match : matches) {
            // 종료된 매칭(ENDED, REJECTED, FORCE_ENDED)은 제외
            if (match.getStatus() == Match.Status.ENDED || 
                match.getStatus() == Match.Status.REJECTED || 
                match.getStatus() == Match.Status.FORCE_ENDED) {
                continue;
            }
            MatchDtos.MatchWithPartnerInfo info = new MatchDtos.MatchWithPartnerInfo();
            info.id = match.getId();
            info.userId = match.getUserId();
            info.trainerId = match.getTrainerId();
            info.status = match.getStatus().name();
            info.requestedBy = match.getRequestedBy().name();
            info.requestedAt = match.getRequestedAt();
            info.acceptedAt = match.getAcceptedAt();
            info.endedAt = match.getEndedAt();
            info.endReason = match.getEndReason();
            info.blocked = match.isBlocked();
            info.blockReason = match.getBlockReason();
            info.reported = match.isReported();
            info.reportReason = match.getReportReason();
            info.createdAt = match.getCreatedAt();
            info.updatedAt = match.getUpdatedAt();

            // 상대방 정보 조회
            if ("USER".equals(role)) {
                // 사용자인 경우: 트레이너 정보 조회
                TrainerProfile trainer = trainerProfileRepository.findById(match.getTrainerId())
                        .orElse(null);
                if (trainer != null) {
                    Account trainerAccount = accountRepository.findById(trainer.getAccountId())
                            .orElse(null);
                    if (trainerAccount != null) {
                        info.partnerName = trainerAccount.getDisplayName();
                        info.partnerUsername = trainerAccount.getUsername();
                        info.isAiTrainer = "ai_trainer".equals(trainerAccount.getUsername());
                    }
                }
            } else if ("TRAINER".equals(role)) {
                // 트레이너인 경우: 사용자 정보 조회
                UserProfile user = userProfileRepository.findById(match.getUserId())
                        .orElse(null);
                if (user != null) {
                    Account userAccount = accountRepository.findById(user.getAccountId())
                            .orElse(null);
                    if (userAccount != null) {
                        info.partnerName = userAccount.getDisplayName();
                        info.partnerUsername = userAccount.getUsername();
                        info.isAiTrainer = false; // 사용자는 AI가 아님
                    }
                }
            }

            result.add(info);
        }

        return ResponseEntity.ok(result);
    }
}
