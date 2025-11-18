package com.healthcare.service;

import com.healthcare.domain.PlanDay;
import com.healthcare.domain.PlanItem;
import com.healthcare.domain.PlanWeek;
import com.healthcare.dto.plan.PlanDtos;
import com.healthcare.repository.MatchRepository;
import com.healthcare.repository.PlanDayRepository;
import com.healthcare.repository.PlanItemRepository;
import com.healthcare.repository.PlanWeekRepository;
import com.healthcare.security.ActorGuard;
import com.healthcare.security.NotFoundException;
import com.healthcare.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class PlanService {

    private final PlanWeekRepository weekRepo;
    private final PlanDayRepository dayRepo;
    private final PlanItemRepository itemRepo;
    private final ActorGuard guard;
    private final SecurityUtil securityUtil;
    private final MatchRepository matchRepo;

    public PlanService(PlanWeekRepository weekRepo,
                       PlanDayRepository dayRepo,
                       PlanItemRepository itemRepo,
                       ActorGuard guard,
                       SecurityUtil securityUtil,
                       MatchRepository matchRepo) {
        this.weekRepo = weekRepo;
        this.dayRepo = dayRepo;
        this.itemRepo = itemRepo;
        this.guard = guard;
        this.securityUtil = securityUtil;
        this.matchRepo = matchRepo;
    }

    /** 주간 플랜 생성 + day 0~6 자동 생성 (트레이너만) */
    @Transactional
    public PlanWeek createWeek(PlanDtos.WeekCreateRequest req) {
        // 권한: 트레이너 + 해당 match에 참여한 트레이너 본인
        guard.requireTrainer();
        
        var match = matchRepo.findById(req.matchId)
                .orElseThrow(() -> new NotFoundException("매칭 정보를 찾을 수 없습니다."));

        guard.requireTrainerOwnsMatch(match.getId());

        var existing = weekRepo.findByMatchIdAndWeekStart(match.getId(), req.weekStart);
        if (existing.isPresent()) {
            ensureWeekDays(existing.get().getId());
            return existing.get();
        }

        var week = new PlanWeek();
        week.setMatchId(match.getId());
        week.setWeekStart(req.weekStart);
        week.setTitle(req.title);
        week.setNote(req.note != null && !req.note.isBlank() ? req.note : null);
        Long creator = req.createdBy != null ? req.createdBy : securityUtil.currentAccountId();
        week.setCreatedBy(creator);

        var saved = weekRepo.save(week);
        ensureWeekDays(saved.getId());
        return saved;
    }

    private void ensureWeekDays(Long weekId) {
        var days = dayRepo.findByWeekIdOrderByDayIndexAsc(weekId);
        if (days != null && !days.isEmpty()) {
            return;
        }
        for (int i = 0; i < 7; i++) {
            var day = new PlanDay();
            day.setWeekId(weekId);
            day.setDayIndex(i);
            day.setNote(null);
            dayRepo.save(day);
        }
    }

    /** 주간 플랜 상세 조회 (days + items) — 매치 참여자(유저/트레이너)만 */
    @Transactional(readOnly = true)
    public PlanDtos.WeekView getWeekView(Long matchId, java.time.LocalDate weekStart) {
        // 접근 가드: 매치 참여자 또는 ADMIN
        guard.requireAccessToMatch(matchId);

        var week = weekRepo.findByMatchIdAndWeekStart(matchId, weekStart)
                .orElseThrow(() -> new com.healthcare.security.NotFoundException("해당 주차 플랜을 찾을 수 없습니다."));
        var days = dayRepo.findByWeekIdOrderByDayIndexAsc(week.getId());

        var dayViews = new ArrayList<PlanDtos.DayView>();
        for (var d : days) {
            var items = itemRepo.findByDayIdOrderByCreatedAtAsc(d.getId());
            var itemViews = new ArrayList<PlanDtos.ItemView>();
            for (var it : items) {
                itemViews.add(new PlanDtos.ItemView(
                        it.getId(),
                        it.getItemType().name(),
                        it.getTitle(),
                        it.getDescription(),
                        it.getTargetKcal(),
                        it.getTargetMin(),
                        it.getStatusMark().name(),
                        it.isLocked()
                ));
            }
            dayViews.add(new PlanDtos.DayView(
                    d.getId(), d.getDayIndex(), d.getNote(), itemViews
            ));
        }
        return new PlanDtos.WeekView(
                week.getId(), week.getMatchId(), week.getWeekStart(), week.getTitle(), week.getNote(), dayViews
        );
    }

    /** Day 메모 수정 (트레이너만) */
    @Transactional
    public PlanDay updateDayNote(Long dayId, String note) {
        // dayId -> matchId 해석 후 소유권 확인
        Long matchId = dayRepo.findMatchIdByDayId(dayId)
                .orElseThrow(() -> new IllegalStateException("Day not found"));
        guard.requireTrainer();
        guard.requireTrainerOwnsMatch(matchId);

        var d = dayRepo.findById(dayId).orElseThrow();
        d.setNote(note);
        return d;
    }

    /** Item 생성 (트레이너만) */
    @Transactional
    public PlanItem createItem(PlanDtos.ItemCreateRequest req) {
        Long matchId = dayRepo.findMatchIdByDayId(req.dayId)
                .orElseThrow(() -> new IllegalStateException("Day not found"));
        guard.requireTrainer();
        guard.requireTrainerOwnsMatch(matchId);

        var d = dayRepo.findById(req.dayId).orElseThrow();
        var item = new PlanItem();
        item.setDayId(d.getId());
        var itemType = PlanItem.ItemType.valueOf(req.itemType.toUpperCase());
        item.setItemType(itemType);
        item.setTitle(req.title);
        item.setDescription(req.description);

        switch (itemType) {
            case DIET -> {
                item.setTargetKcal(req.targetKcal);
                item.setTargetMin(null);
            }
            case WORKOUT -> {
                item.setTargetKcal(null);
                item.setTargetMin(req.targetMin);
            }
            case NOTE -> {
                item.setTargetKcal(null);
                item.setTargetMin(null);
            }
        }
        return itemRepo.save(item);
    }

    /** Item 수정 (locked면 불가, 트레이너만) */
    @Transactional
    public PlanItem updateItem(Long itemId, PlanDtos.ItemUpdateRequest req) {
        Long matchId = itemRepo.findMatchIdByItemId(itemId)
                .orElseThrow(() -> new IllegalStateException("Item not found"));
        guard.requireTrainer();
        guard.requireTrainerOwnsMatch(matchId);

        var item = itemRepo.findById(itemId).orElseThrow();
        if (item.isLocked()) throw new IllegalStateException("Item locked");
        if (req.title != null) item.setTitle(req.title);
        if (req.description != null) item.setDescription(req.description);
        if (req.targetKcal != null) item.setTargetKcal(req.targetKcal);
        if (req.targetMin != null) item.setTargetMin(req.targetMin);

        switch (item.getItemType()) {
            case DIET -> item.setTargetMin(null);
            case WORKOUT -> item.setTargetKcal(null);
            case NOTE -> {
                item.setTargetKcal(null);
                item.setTargetMin(null);
            }
        }
        return item;
    }

    /** Item 상태 변경 (O/D/X), lockAfterComplete=true면 잠금 — 유저만 */
    @Transactional
    public PlanItem changeItemStatus(Long itemId, String status, Boolean lockAfterComplete) {
        Long matchId = itemRepo.findMatchIdByItemId(itemId)
                .orElseThrow(() -> new IllegalStateException("Item not found"));
        guard.requireUser();
        guard.requireUserOwnsMatch(matchId); // 매치의 당사자 유저만 완료 체크 가능

        var item = itemRepo.findById(itemId).orElseThrow();
        if (item.isLocked()) throw new IllegalStateException("Item locked");

        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("statusMark is required");
        }

        PlanItem.StatusMark mark;
        try {
            mark = PlanItem.StatusMark.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("statusMark must be O, D, or X");
        }

        item.setStatusMark(mark);
        item.setCompletedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(lockAfterComplete)) {
            item.setLocked(true);
        }
        return item;
    }

    /** Item 잠금/해제 (트레이너만) */
    @Transactional
    public PlanItem setItemLock(Long itemId, Boolean locked) {
        Long matchId = itemRepo.findMatchIdByItemId(itemId)
                .orElseThrow(() -> new IllegalStateException("Item not found"));
        guard.requireTrainer();
        guard.requireTrainerOwnsMatch(matchId);

        var item = itemRepo.findById(itemId).orElseThrow();
        item.setLocked(Boolean.TRUE.equals(locked));
        return item;
    }

    /** Item 삭제 (locked면 불가, 트레이너만) */
    @Transactional
    public void deleteItem(Long itemId) {
        Long matchId = itemRepo.findMatchIdByItemId(itemId)
                .orElseThrow(() -> new IllegalStateException("Item not found"));
        guard.requireTrainer();
        guard.requireTrainerOwnsMatch(matchId);

        var item = itemRepo.findById(itemId).orElseThrow();
        if (item.isLocked()) throw new IllegalStateException("Item locked");
        itemRepo.delete(item);
    }
}
