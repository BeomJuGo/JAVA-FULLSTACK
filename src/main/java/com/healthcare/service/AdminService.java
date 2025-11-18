package com.healthcare.service;

import com.healthcare.domain.Account;
import com.healthcare.domain.AdminActionLog;
import com.healthcare.repository.AccountRepository;
import com.healthcare.repository.AdminActionLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final AdminActionLogRepository logRepo;
    private final AccountRepository accountRepo;
    private final CommunityService communityService;
    private final MatchService matchService;

    public AdminService(AdminActionLogRepository logRepo, AccountRepository accountRepo,
                        CommunityService communityService, MatchService matchService) {
        this.logRepo = logRepo;
        this.accountRepo = accountRepo;
        this.communityService = communityService;
        this.matchService = matchService;
    }

    private void log(Long adminAcc, AdminActionLog.ActionType actionType,
                     AdminActionLog.TargetType targetType, Long targetId, String reason) {
        var log = new AdminActionLog();
        log.setAdminAcc(adminAcc);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setReason(reason);
        logRepo.save(log);
    }

    /* ---- Posts / Comments ---- */

    @Transactional
    public void setPostHidden(Long adminAcc, Long postId, boolean hidden, String reason) {
        communityService.hidePost(postId, hidden, reason);
        log(adminAcc,
                hidden ? AdminActionLog.ActionType.HIDE_POST : AdminActionLog.ActionType.UNHIDE_POST,
                AdminActionLog.TargetType.POST, postId, reason);
    }

    @Transactional
    public void setCommentHidden(Long adminAcc, Long commentId, boolean hidden, String reason) {
        communityService.hideComment(commentId, hidden, reason);
        log(adminAcc,
                hidden ? AdminActionLog.ActionType.HIDE_COMMENT : AdminActionLog.ActionType.UNHIDE_COMMENT,
                AdminActionLog.TargetType.COMMENT, commentId, reason);
    }

    /* ---- Accounts ---- */

    @Transactional
    public void suspendAccount(Long adminAcc, Long accountId, String reason) {
        var acc = accountRepo.findById(accountId).orElseThrow();
        acc.setStatus(Account.Status.SUSPENDED);
        log(adminAcc, AdminActionLog.ActionType.SUSPEND_ACCOUNT, AdminActionLog.TargetType.ACCOUNT, accountId, reason);
    }

    @Transactional
    public void restoreAccount(Long adminAcc, Long accountId, String reason) {
        var acc = accountRepo.findById(accountId).orElseThrow();
        acc.setStatus(Account.Status.ACTIVE);
        log(adminAcc, AdminActionLog.ActionType.RESTORE_ACCOUNT, AdminActionLog.TargetType.ACCOUNT, accountId, reason);
    }

    /* ---- Matches ---- */

    @Transactional
    public void forceEndMatch(Long adminAcc, Long matchId, String reason) {
        matchService.forceEnd(matchId, reason); // 기존 로직 사용
        log(adminAcc, AdminActionLog.ActionType.FORCE_END_MATCH, AdminActionLog.TargetType.MATCH, matchId, reason);
    }

    /* ---- Logs ---- */

    @Transactional(readOnly = true)
    public Page<AdminActionLog> pageLogs(int page, int size) {
        return logRepo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }
}
