package com.healthcare.dto.plan;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

/** 플랜 DTO 모음 */
public class PlanDtos {

    // ====== Commands ======

    /** 주간 플랜 생성 요청 */
    public static class WeekCreateRequest {
        @NotNull(message = "matchId는 필수입니다.")
        public Long matchId;

        @NotNull(message = "weekStart는 필수입니다. (yyyy-MM-dd)")
        public LocalDate weekStart;

        @NotBlank(message = "title은 비어있을 수 없습니다.")
        @Size(max = 100, message = "title은 100자 이하여야 합니다.")
        public String title;

        @Size(max = 500, message = "note는 500자 이하여야 합니다.")
        public String note; // optional, null 또는 빈 문자열 가능

        public Long createdBy; // optional, 서버에서 현재 계정으로 덮어씀
    }

    /** Day 메모 수정 */
    public static class DayUpdateRequest {
        @Size(max = 500, message = "note는 500자 이하여야 합니다.")
        public String note;
    }

    /** Item 생성 */
    public static class ItemCreateRequest {
        public Long dayId;

        @NotBlank(message = "itemType은 비어있을 수 없습니다.")
        @Pattern(regexp = "^(WORKOUT|DIET|NOTE)$", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "itemType은 WORKOUT / DIET / NOTE 중 하나여야 합니다.")
        public String itemType;

        @NotBlank(message = "title은 비어있을 수 없습니다.")
        @Size(max = 100, message = "title은 100자 이하여야 합니다.")
        public String title;

        @Size(max = 1000, message = "description은 1000자 이하여야 합니다.")
        public String description;

        @PositiveOrZero(message = "targetKcal은 0 이상이어야 합니다.")
        @Max(value = 10000, message = "targetKcal은 10000 이하여야 합니다.")
        public Integer targetKcal;

        @PositiveOrZero(message = "targetMin은 0 이상이어야 합니다.")
        @Max(value = 1440, message = "targetMin은 1440분 이하여야 합니다.")
        public Integer targetMin;
    }

    /** Item 수정 (필드 선택적) */
    public static class ItemUpdateRequest {
        @Size(max = 100, message = "title은 100자 이하여야 합니다.")
        public String title;

        @Size(max = 1000, message = "description은 1000자 이하여야 합니다.")
        public String description;

        @PositiveOrZero(message = "targetKcal은 0 이상이어야 합니다.")
        @Max(value = 10000, message = "targetKcal은 10000 이하여야 합니다.")
        public Integer targetKcal;

        @PositiveOrZero(message = "targetMin은 0 이상이어야 합니다.")
        @Max(value = 1440, message = "targetMin은 1440분 이하여야 합니다.")
        public Integer targetMin;
    }

    /** Item 상태 변경 (유저 완료 체크용) */
    public static class ItemStatusRequest {
        @NotBlank(message = "statusMark는 필수입니다.")
        @Pattern(regexp = "^(O|D|X)$", message = "statusMark는 O, D, X 중 하나여야 합니다.")
        public String statusMark;

        /** 완료 시 잠글지 여부 (선택) */
        public Boolean lockAfterComplete;
    }

    /** Item 잠금/해제 (트레이너용) */
    public static class ItemLockRequest {
        @NotNull(message = "locked는 필수입니다.")
        public Boolean locked;
    }

    // ====== Views ======

    public static class WeekView {
        public Long id;
        public Long matchId;
        public LocalDate weekStart;
        public String title;
        public String note;
        public List<DayView> days;

        public WeekView(Long id, Long matchId, LocalDate weekStart, String title, String note, List<DayView> days) {
            this.id = id;
            this.matchId = matchId;
            this.weekStart = weekStart;
            this.title = title;
            this.note = note;
            this.days = days;
        }
    }

    public static class DayView {
        public Long id;
        public Integer dayIndex;
        public String note;
        public List<ItemView> items;

        public DayView(Long id, Integer dayIndex, String note, List<ItemView> items) {
            this.id = id;
            this.dayIndex = dayIndex;
            this.note = note;
            this.items = items;
        }
    }

    public static class ItemView {
        public Long id;
        public String itemType;
        public String title;
        public String description;
        public Integer targetKcal;
        public Integer targetMin;
        public String statusMark;
        public boolean locked;

        public ItemView(Long id, String itemType, String title, String description,
                        Integer targetKcal, Integer targetMin, String statusMark, boolean locked) {
            this.id = id;
            this.itemType = itemType;
            this.title = title;
            this.description = description;
            this.targetKcal = targetKcal;
            this.targetMin = targetMin;
            this.statusMark = statusMark;
            this.locked = locked;
        }
    }
}
