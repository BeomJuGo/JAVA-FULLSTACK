package com.healthcare.web;

import com.healthcare.dto.admin.AdminDtos;
import com.healthcare.security.SecurityUtil;
import com.healthcare.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService service;
    private final SecurityUtil securityUtil;

    public AdminController(AdminService service, SecurityUtil securityUtil) {
        this.service = service;
        this.securityUtil = securityUtil;
    }

    /* ---- Posts ---- */

    @PostMapping("/posts/{postId}/hide")
    public ResponseEntity<Void> hidePost(@PathVariable Long postId, @RequestBody AdminDtos.HidePostRequest req) {
        Long adminAcc = securityUtil.currentAccountId();
        service.setPostHidden(adminAcc, postId, req.hidden, req.reason);
        return ResponseEntity.noContent().build();
    }

    /* ---- Comments ---- */

    @PostMapping("/comments/{commentId}/hide")
    public ResponseEntity<Void> hideComment(@PathVariable Long commentId, @RequestBody AdminDtos.HideCommentRequest req) {
        Long adminAcc = securityUtil.currentAccountId();
        service.setCommentHidden(adminAcc, commentId, req.hidden, req.reason);
        return ResponseEntity.noContent().build();
    }

    /* ---- Accounts ---- */

    @PostMapping("/accounts/{accountId}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable Long accountId, @RequestBody AdminDtos.SuspendAccountRequest req) {
        Long adminAcc = securityUtil.currentAccountId();
        service.suspendAccount(adminAcc, accountId, req.reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts/{accountId}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long accountId, @RequestBody AdminDtos.RestoreAccountRequest req) {
        Long adminAcc = securityUtil.currentAccountId();
        service.restoreAccount(adminAcc, accountId, req.reason);
        return ResponseEntity.noContent().build();
    }

    /* ---- Matches ---- */

    @PostMapping("/matches/{matchId}/force-end")
    public ResponseEntity<Void> forceEnd(@PathVariable Long matchId, @RequestBody AdminDtos.ForceEndMatchRequest req) {
        Long adminAcc = securityUtil.currentAccountId();
        service.forceEndMatch(adminAcc, matchId, req.reason);
        return ResponseEntity.noContent().build();
    }

    /* ---- Logs ---- */

    @GetMapping("/logs")
    public ResponseEntity<AdminDtos.PageResponse<AdminDtos.LogView>> logs(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size) {

        var pg = service.pageLogs(page, size);
        var list = pg.getContent().stream().map(l ->
                new AdminDtos.LogView(l.getId(), l.getAdminAcc(), l.getActionType().name(),
                        l.getTargetType().name(), l.getTargetId(), l.getReason(), l.getCreatedAt())
        ).collect(Collectors.toList());

        return ResponseEntity.ok(new AdminDtos.PageResponse<>(
                list, pg.getNumber(), pg.getSize(), pg.getTotalElements(), pg.getTotalPages(), pg.isLast()
        ));
    }
}
