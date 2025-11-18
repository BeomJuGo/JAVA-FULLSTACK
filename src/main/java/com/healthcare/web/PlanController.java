package com.healthcare.web;

import com.healthcare.dto.plan.PlanDtos;
import com.healthcare.service.PlanService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    /** 주간 상세 조회 (참여자만 접근) */
    @GetMapping("/weeks")
    public ResponseEntity<PlanDtos.WeekView> getWeekView(
            @RequestParam Long matchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        return ResponseEntity.ok(service.getWeekView(matchId, weekStart));
    }

    /** 트레이너 전용: 주간 생성 */
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/weeks")
    public ResponseEntity<Long> createWeek(@Valid @RequestBody PlanDtos.WeekCreateRequest req) {
        var w = service.createWeek(req);
        return ResponseEntity.ok(w.getId());
    }

    /** 트레이너 전용: Day 메모 수정 */
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/days/{dayId}")
    public ResponseEntity<Long> updateDay(@PathVariable Long dayId, @Valid @RequestBody PlanDtos.DayUpdateRequest req) {
        var d = service.updateDayNote(dayId, req.note);
        return ResponseEntity.ok(d.getId());
    }

    /** 트레이너 전용: Item 생성 */
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/days/{dayId}/items")
    public ResponseEntity<Long> createItem(@PathVariable Long dayId, @Valid @RequestBody PlanDtos.ItemCreateRequest req) {
        req.dayId = dayId;
        var item = service.createItem(req);
        return ResponseEntity.ok(item.getId());
    }

    /** 트레이너 전용: Item 수정 */
    @PreAuthorize("hasRole('TRAINER')")
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<Long> updateItem(@PathVariable Long itemId, @Valid @RequestBody PlanDtos.ItemUpdateRequest req) {
        var it = service.updateItem(itemId, req);
        return ResponseEntity.ok(it.getId());
    }

    /** 유저 전용: Item 상태 변경 (O/D/X) + 선택 잠금 */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/items/{itemId}/status")
    public ResponseEntity<Long> changeStatus(@PathVariable Long itemId, @Valid @RequestBody PlanDtos.ItemStatusRequest req) {
        var it = service.changeItemStatus(itemId, req.statusMark, req.lockAfterComplete);
        return ResponseEntity.ok(it.getId());
    }

    /** 트레이너 전용: Item 잠금/해제 */
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/items/{itemId}/lock")
    public ResponseEntity<Long> lockItem(@PathVariable Long itemId, @Valid @RequestBody PlanDtos.ItemLockRequest req) {
        var it = service.setItemLock(itemId, req.locked);
        return ResponseEntity.ok(it.getId());
    }

    /** 트레이너 전용: Item 삭제 */
    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        service.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }
}
