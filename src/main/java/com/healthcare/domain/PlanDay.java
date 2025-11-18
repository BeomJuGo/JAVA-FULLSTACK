package com.healthcare.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "plan_day", uniqueConstraints = {
        @UniqueConstraint(name="uc_plan_day_week_idx", columnNames={"week_id","day_index"})
})
public class PlanDay {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="week_id", nullable = false)
    private Long weekId;

    @Column(name="day_index", nullable = false)
    private Integer dayIndex; // 0~6

    @Column(length = 1000)
    private String note;

    // getters/setters
    public Long getId() { return id; }
    public Long getWeekId() { return weekId; }
    public void setWeekId(Long weekId) { this.weekId = weekId; }
    public Integer getDayIndex() { return dayIndex; }
    public void setDayIndex(Integer dayIndex) { this.dayIndex = dayIndex; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
