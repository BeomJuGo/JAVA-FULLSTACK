package com.healthcare.security;

import com.healthcare.repository.TrainerProfileRepository;
import com.healthcare.repository.UserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ActorResolver {

    private final SecurityUtil securityUtil;
    private final UserProfileRepository userProfileRepo;
    private final TrainerProfileRepository trainerProfileRepo;

    public ActorResolver(SecurityUtil securityUtil,
                         UserProfileRepository userProfileRepo,
                         TrainerProfileRepository trainerProfileRepo) {
        this.securityUtil = securityUtil;
        this.userProfileRepo = userProfileRepo;
        this.trainerProfileRepo = trainerProfileRepo;
    }

    public ActorContext resolve() {
        Long accountId = securityUtil.getCurrentAccountId();
        Set<String> roles = securityUtil.getCurrentRoles();

        Long userId = userProfileRepo.findByAccountId(accountId)
                .map(up -> up.getId())
                .orElse(null);

        Long trainerId = trainerProfileRepo.findByAccountId(accountId)
                .map(tp -> tp.getId())
                .orElse(null);

        return new ActorContext(accountId, userId, trainerId, roles);
    }
}
