package com.healthcare.security;

import com.healthcare.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActorGuard {

    private final ActorResolver actorResolver;
    private final MatchRepository matchRepository;

    public void requireTrainer() {
        if (!actorResolver.resolve().isTrainer())
            throw new ForbiddenException("트레이너 권한이 필요합니다.");
    }

    public void requireUser() {
        if (!actorResolver.resolve().isUser())
            throw new ForbiddenException("유저 권한이 필요합니다.");
    }

    /** 조회/접근: 매치의 유저 또는 트레이너 또는 ADMIN */
    public void requireAccessToMatch(Long matchId) {
        var actor = actorResolver.resolve();
        if (actor.isAdmin()) return;

        Long uid = matchRepository.findUserProfileIdByMatchId(matchId)
                .orElseThrow(() -> new NotFoundException("매치를 찾을 수 없습니다."));
        Long tid = matchRepository.findTrainerProfileIdByMatchId(matchId)
                .orElseThrow(() -> new NotFoundException("매치를 찾을 수 없습니다."));

        boolean ok = (actor.userProfileId() != null && actor.userProfileId().equals(uid))
                || (actor.trainerProfileId() != null && actor.trainerProfileId().equals(tid));
        if (!ok) throw new ForbiddenException("접근 권한이 없습니다.");
    }

    /** 트레이너 전용 소유권 */
    public void requireTrainerOwnsMatch(Long matchId) {
        var actor = actorResolver.resolve();
        if (!actor.isTrainer()) throw new ForbiddenException("트레이너 권한이 필요합니다.");
        Long tid = matchRepository.findTrainerProfileIdByMatchId(matchId)
                .orElseThrow(() -> new NotFoundException("매치를 찾을 수 없습니다."));
        if (!tid.equals(actor.trainerProfileId()))
            throw new ForbiddenException("본인 매치에만 수정할 수 있습니다.");
    }

    /** 유저 전용 소유권 */
    public void requireUserOwnsMatch(Long matchId) {
        var actor = actorResolver.resolve();
        if (!actor.isUser()) throw new ForbiddenException("유저 권한이 필요합니다.");
        Long uid = matchRepository.findUserProfileIdByMatchId(matchId)
                .orElseThrow(() -> new NotFoundException("매치를 찾을 수 없습니다."));
        if (!uid.equals(actor.userProfileId()))
            throw new ForbiddenException("본인 매치에만 완료 체크할 수 있습니다.");
    }
}
